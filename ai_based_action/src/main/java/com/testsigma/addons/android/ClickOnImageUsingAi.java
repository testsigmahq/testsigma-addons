package com.testsigma.addons.android;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.util.Collections;

@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and tap a UI element on an Android device using a single AI call. " +
                "AI reports the image dimensions it analyzed; coordinates are scaled back to device space automatically. " +
                "Fails if the element is not found.",
        displayName = "Ai: Click on Image/text matching prompt",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = true)
public class ClickOnImageUsingAi extends AndroidAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== ClickOnImageUsingAi (Android): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Device screenshot size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_android_capture", logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.LOCATE_PROMPT_ANDROID, query, logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get image response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean found = responseNode.path("found").asBoolean(false);
            if (!found) {
                String reason = responseNode.path("description").asText("element not found");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                setErrorMessage("AI could not locate '" + query + "': " + reason);
                return Result.FAILED;
            }

            int aiX1        = responseNode.path("x1").asInt(0);
            int aiY1        = responseNode.path("y1").asInt(0);
            int aiX2        = responseNode.path("x2").asInt(0);
            int aiY2        = responseNode.path("y2").asInt(0);
            int imageWidth  = responseNode.path("imageWidth").asInt(0);
            int imageHeight = responseNode.path("imageHeight").asInt(0);
            int confidence  = responseNode.path("confidence").asInt(50);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI result — bbox: (%d,%d)-(%d,%d) | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, confidence, description));

            if (imageWidth <= 0 || imageHeight <= 0) {
                setErrorMessage(String.format(
                        "AI returned invalid image dimensions (imageWidth=%d, imageHeight=%d).",
                        imageWidth, imageHeight));
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
            int tapX = (cap[0] + cap[2]) / 2;
            int tapY = (cap[1] + cap[3]) / 2;
            logger.info(String.format(
                    "Device bbox: (%d,%d)-(%d,%d)  tap: (%d,%d)",
                    cap[0], cap[1], cap[2], cap[3], tapX, tapY));

            BufferedImage annotated = AiActionUtils.drawHighlight(
                    pageCapture, cap[0], cap[1], cap[2], cap[3], tapX, tapY, Color.MAGENTA);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            logger.info(String.format("Tapping at (%d,%d)  confidence=%d", tapX, tapY, confidence));
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), tapX, tapY));
            tap.addAction(finger.createPointerDown(0));
            tap.addAction(new Pause(finger, Duration.ofMillis(100)));
            tap.addAction(finger.createPointerUp(0));
            ((Interactive) driver).perform(Collections.singletonList(tap));

            setSuccessMessage(String.format(
                    "Successfully tapped '%s' at (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, tapX, tapY, cap[0], cap[1], cap[2], cap[3], confidence, description));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to tap using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
