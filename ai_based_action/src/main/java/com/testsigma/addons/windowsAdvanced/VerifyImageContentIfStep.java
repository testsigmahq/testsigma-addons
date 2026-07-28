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
@Action(actionText = "Ai: Verify if the page has content matching prompt verification-query",
        description = "Capture a screenshot of the desktop and ask AI to verify whether the described " +
                "content or condition is present. The step passes if AI confirms the query; fails otherwise. " +
                "Use natural language to describe what you expect to see.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        actionType = StepActionType.IF_CONDITION,
        useCustomScreenshot = true)
public class VerifyImageContentIfStep extends WindowsAdvancedAction {

    @TestData(reference = "verification-query")
    private com.testsigma.sdk.TestData verificationQuery;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== VerifyImageContentIfStep (WindowsAdvanced): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {
            String query = verificationQuery.getValue().toString();
            logger.info("Verification query: " + query);

            Dimension logicalScreen = Toolkit.getDefaultToolkit().getScreenSize();
            int logicalScreenW = logicalScreen.width;
            int logicalScreenH = logicalScreen.height;
            logger.info("Logical screen size (Toolkit): " + logicalScreenW + "x" + logicalScreenH);

            BufferedImage desktopCapture = captureScreenshotWithRobot();
            int captureW = desktopCapture.getWidth();
            int captureH = desktopCapture.getHeight();
            logger.info("Robot desktop capture size: " + captureW + "x" + captureH);

            double displayScaleX = (double) captureW / logicalScreen.width;
            double displayScaleY = (double) captureH / logicalScreen.height;
            logger.info(String.format(
                    "Display scale (capture / logical): %.4fx%.4f", displayScaleX, displayScaleY));

            screenshotFile = AiActionUtils.captureAsJpeg(desktopCapture, "ai_desktop_verify_capture", logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.VERIFY_PROMPT_DESKTOP, query,
                    logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get verification response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_verify_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean verified   = responseNode.path("verified").asBoolean(false);
            int aiX1           = responseNode.path("x1").asInt(0);
            int aiY1           = responseNode.path("y1").asInt(0);
            int aiX2           = responseNode.path("x2").asInt(0);
            int aiY2           = responseNode.path("y2").asInt(0);
            int imageWidth     = responseNode.path("imageWidth").asInt(0);
            int imageHeight    = responseNode.path("imageHeight").asInt(0);
            int confidence     = responseNode.path("confidence").asInt(0);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI verification result — verified=%b | bbox: (%d,%d)-(%d,%d) | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    verified, aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, confidence, description));

            if (verified && imageWidth > 0 && imageHeight > 0 && (aiX1 | aiY1 | aiX2 | aiY2) != 0) {
                int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
                int capCX = (cap[0] + cap[2]) / 2;
                int capCY = (cap[1] + cap[3]) / 2;
                BufferedImage annotated = AiActionUtils.drawHighlight(
                        desktopCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.GREEN);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_verify_passed");
            } else {
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_verify_result");
            }
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            if (verified) {
                setSuccessMessage(String.format(
                        "Verification PASSED for '%s' | confidence=%d | %s",
                        query, confidence, description));
                return Result.SUCCESS;
            } else {
                setErrorMessage(String.format(
                        "Verification FAILED for '%s' | confidence=%d | %s",
                        query, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to verify using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }

    private BufferedImage captureScreenshotWithRobot() throws AWTException {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Robot robot = new Robot();
        BufferedImage shot = robot.createScreenCapture(new Rectangle(screen));
        if (shot.getWidth() != screen.width || shot.getHeight() != screen.height) {
            logger.info(String.format(
                    "Robot capture %dx%d differs from logical screen %dx%d (HiDPI) — resizing to " +
                            "logical size so coordinates match the AI coords.",
                    shot.getWidth(), shot.getHeight(), screen.width, screen.height));
            shot = AiActionUtils.resizeImage(shot, screen.width, screen.height);
        }
        return shot;
    }
}
