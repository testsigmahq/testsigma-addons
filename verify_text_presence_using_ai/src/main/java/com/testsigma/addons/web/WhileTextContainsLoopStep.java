package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

@Data
@Action(actionText = "Ai: While text containing test-data is condition on the screen",
        description = "Capture a screenshot and ask AI to check whether any visible text on the screen " +
                "contains the substring specified in test-data. Partial/substring matching is used. " +
                "When condition is 'visible': continues the loop while matching text IS visible, exits " +
                "when it disappears. When condition is 'not visible': continues the loop while no " +
                "matching text is present, exits when text containing the substring appears.",
        applicationType = ApplicationType.WEB,
        actionType = StepActionType.WHILE_LOOP,
        useCustomScreenshot = true)
public class WhileTextContainsLoopStep extends WebAction {

    private static final int LOW_CONFIDENCE_THRESHOLD = 30;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "condition",
              allowedValues = {"visible", "not visible"})
    private com.testsigma.sdk.TestData condition;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== WhileTextContains (LOOP): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;
        BufferedImage pageCapture = null;

        try {
            // ── Input validation ──────────────────────────────────────────────
            if (testData == null || testData.getValue() == null
                    || StringUtils.isBlank(testData.getValue().toString())) {
                setErrorMessage("test-data is empty. Please provide the text substring to check.");
                return Result.FAILED;
            }
            if (condition == null || condition.getValue() == null
                    || StringUtils.isBlank(condition.getValue().toString())) {
                setErrorMessage("condition is empty. Allowed values: 'visible', 'not visible'.");
                return Result.FAILED;
            }
            String text = testData.getValue().toString().trim();
            String cond = condition.getValue().toString().trim();
            if (!StringUtils.equalsAnyIgnoreCase(cond, "visible", "not visible")) {
                setErrorMessage("Invalid condition value: \"" + cond
                        + "\". Allowed values: 'visible', 'not visible'.");
                return Result.FAILED;
            }
            boolean expectVisible = StringUtils.equalsIgnoreCase(cond, "visible");
            logger.info(String.format("While loop — text contains='%s', condition='%s'", text, cond));

            // ── Screenshot ────────────────────────────────────────────────────
            try {
                byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            } catch (Exception e) {
                logger.info("Screenshot capture failed:\n" + ExceptionUtils.getStackTrace(e));
                setErrorMessage("Failed to capture screenshot from browser: " + e.getMessage());
                return Result.FAILED;
            }
            if (pageCapture == null) {
                setErrorMessage("Screenshot image could not be decoded (null image). Check browser state.");
                return Result.FAILED;
            }
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Screenshot size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_while_contains_capture", logger);

            // ── AI invocation ─────────────────────────────────────────────────
            String query = "Is there any visible text on the screen that contains or includes the substring \""
                    + text + "\"? " +
                    "Partial matches count — if any word, sentence, label, or text element on screen includes " +
                    "this substring anywhere within it, set verified=true and highlight that text element.";

            String aiResponse = AiActionUtils.invokeAiAnthropicFirst(
                    ai, screenshotFile, AiActionUtils.VERIFY_PROMPT_WEB, query, logger);

            if (StringUtils.isBlank(aiResponse)) {
                setErrorMessage("AI returned an empty response for text: \"" + text +
                        "\". Both primary and fallback models failed. Please retry.");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_while_contains_error");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            // ── Parse response ────────────────────────────────────────────────
            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("AI response could not be parsed as JSON for text: \"" + text +
                        "\". Raw response (truncated): " + StringUtils.truncate(aiResponse, 300));
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_while_contains_error");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean textVisible = responseNode.path("verified").asBoolean(false);
            int aiX1            = responseNode.path("x1").asInt(0);
            int aiY1            = responseNode.path("y1").asInt(0);
            int aiX2            = responseNode.path("x2").asInt(0);
            int aiY2            = responseNode.path("y2").asInt(0);
            int imageWidth      = responseNode.path("imageWidth").asInt(0);
            int imageHeight     = responseNode.path("imageHeight").asInt(0);
            int confidence      = responseNode.path("confidence").asInt(0);
            String description  = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI result — textVisible=%b | confidence=%d | desc='%s'",
                    textVisible, confidence, description));

            if (confidence < LOW_CONFIDENCE_THRESHOLD) {
                logger.info("WARNING: Low AI confidence (" + confidence +
                        "%). Loop condition may be unreliable for text: \"" + text + "\"");
            }

            // ── Annotate screenshot ───────────────────────────────────────────
            if (textVisible && imageWidth > 0 && imageHeight > 0 && (aiX1 | aiY1 | aiX2 | aiY2) != 0) {
                int[] cap = AiActionUtils.scaleAiToCapture(
                        aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
                int capCX = (cap[0] + cap[2]) / 2;
                int capCY = (cap[1] + cap[3]) / 2;
                BufferedImage annotated = AiActionUtils.drawHighlight(
                        pageCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.GREEN);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_while_contains_found");
            } else {
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_while_contains_result");
            }
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            // ── Evaluate loop condition ───────────────────────────────────────
            // SUCCESS = condition still holds → continue loop
            // FAILED  = condition no longer holds → exit loop
            boolean conditionMet = (expectVisible == textVisible);
            String visibilityStatus = textVisible ? "visible" : "not visible";

            if (conditionMet) {
                setSuccessMessage(String.format(
                        "Text containing \"%s\" is %s — continuing loop | confidence=%d | %s",
                        text, visibilityStatus, confidence, description));
                return Result.SUCCESS;
            } else {
                setSuccessMessage(String.format(
                        "Text containing \"%s\" is %s — loop condition no longer met, exiting | confidence=%d | %s",
                        text, visibilityStatus, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Unexpected exception:\n" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unexpected error while evaluating while-text-contains condition: " + ExceptionUtils.getMessage(e));
            if (pageCapture != null && finalAnnotatedFile == null) {
                try {
                    finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(
                            pageCapture, "ai_while_contains_exception");
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                } catch (Exception ignored) { }
            }
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
