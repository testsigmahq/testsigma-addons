package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Data
@Action(actionText = "Ai: Hover on Image/text matching prompt prompt-describing-image",
        description = "Locate a UI element using AI and hover the mouse over it. " +
                "Fails if the element is not found.",
        displayName = "Ai: Hover on Image/text matching prompt",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = true)
public class HoverOnImageUsingAi extends WindowsAdvancedAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== HoverOnImageUsingAi (WindowsAdvanced): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            // Determine logical screen size (OS-level DIP coordinates used by Robot.mouseMove)
            Dimension logicalScreen = Toolkit.getDefaultToolkit().getScreenSize();
            int logicalScreenW = logicalScreen.width;
            int logicalScreenH = logicalScreen.height;
            logger.info("Logical screen size (Toolkit): " + logicalScreenW + "x" + logicalScreenH);

            // Robot.createScreenCapture returns physical pixels on HiDPI displays
            Robot robot = new Robot();
            BufferedImage desktopCapture = robot.createScreenCapture(new Rectangle(logicalScreen));
            int captureW = desktopCapture.getWidth();
            int captureH = desktopCapture.getHeight();
            logger.info("Robot desktop capture size (physical px): " + captureW + "x" + captureH);

            // Display scale factor: physical / logical
            double displayScaleX = (double) captureW / logicalScreenW;
            double displayScaleY = (double) captureH / logicalScreenH;
            logger.info(String.format(
                    "Display scale (capture / logical): %.4fx%.4f", displayScaleX, displayScaleY));

            screenshotFile = AiActionUtils.captureAsJpeg(desktopCapture, "ai_desktop_capture", logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.LOCATE_PROMPT_DESKTOP, query, logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get image response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_hover_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean found = responseNode.path("found").asBoolean(false);
            if (!found) {
                String reason = responseNode.path("description").asText("element not found");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_hover_failed");
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
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_hover_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            // Stage A: AI image coords → capture pixel coords
            int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
            int capCX = (cap[0] + cap[2]) / 2;
            int capCY = (cap[1] + cap[3]) / 2;
            logger.info(String.format(
                    "Capture-pixel bbox: (%d,%d)-(%d,%d)  center: (%d,%d)",
                    cap[0], cap[1], cap[2], cap[3], capCX, capCY));

            // Stage B: capture pixels → logical screen coords (divide by display scale)
            int logicalCX = (int) Math.round(capCX / displayScaleX);
            int logicalCY = (int) Math.round(capCY / displayScaleY);
            logger.info(String.format(
                    "Logical screen center: (%d,%d)", logicalCX, logicalCY));

            // Annotate in capture pixel space
            BufferedImage annotated = AiActionUtils.drawHighlight(
                    desktopCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.MAGENTA);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_hover_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            logger.info(String.format("Hovering via Robot at logical (%d,%d)  confidence=%d", logicalCX, logicalCY, confidence));
            robot.mouseMove(logicalCX, logicalCY);

            setSuccessMessage(String.format(
                    "Successfully hovered over '%s' at logical (%d,%d) | capture bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, logicalCX, logicalCY, cap[0], cap[1], cap[2], cap[3], confidence, description));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to hover using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
