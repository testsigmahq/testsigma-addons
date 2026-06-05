package com.testsigma.addons.windows;

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
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

@Data
@Action(actionText = "Ai: Hover on Image/text matching prompt prompt-describing-image",
        description = "Locate a UI element on a Windows application using AI and hover over it. " +
                "Fails if the element is not found.",
        displayName = "Ai: Hover on Image/text matching prompt",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class HoverOnImageUsingAi extends WindowsAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== HoverOnImageUsingAi (Windows): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Driver screenshot size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_win_capture", logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.LOCATE_PROMPT_DESKTOP, query, logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get image response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_hover_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean found = responseNode.path("found").asBoolean(false);
            if (!found) {
                String reason = responseNode.path("description").asText("element not found");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_hover_failed");
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
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_hover_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
            int logicalCX = (cap[0] + cap[2]) / 2;
            int logicalCY = (cap[1] + cap[3]) / 2;
            logger.info(String.format(
                    "Logical bbox: (%d,%d)-(%d,%d)  center: (%d,%d)",
                    cap[0], cap[1], cap[2], cap[3], logicalCX, logicalCY));

            BufferedImage annotated = AiActionUtils.drawHighlight(
                    pageCapture, cap[0], cap[1], cap[2], cap[3], logicalCX, logicalCY, Color.MAGENTA);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_hover_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            logger.info(String.format("Hovering at (%d,%d)  confidence=%d", logicalCX, logicalCY, confidence));
            new Actions(driver).moveToLocation(logicalCX, logicalCY).perform();

            setSuccessMessage(String.format(
                    "Successfully hovered over '%s' at (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, logicalCX, logicalCY, cap[0], cap[1], cap[2], cap[3], confidence, description));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to hover using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
