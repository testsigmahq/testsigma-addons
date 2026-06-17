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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

@Data
@Action(actionText = "Ai: Verify screen contains verification-query",
        description = "Capture a screenshot of the Windows application and ask AI to verify whether the described " +
                "content or condition is present. The step passes if AI confirms the query; fails otherwise.",
        displayName = "Ai: Verify screen contains",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class VerifyImageContent extends WindowsAction {

    @TestData(reference = "verification-query")
    private com.testsigma.sdk.TestData verificationQuery;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== VerifyImageContent (Windows): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {
            String query = verificationQuery.getValue().toString();
            logger.info("Verification query: " + query);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Driver screenshot size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_win_verify_capture", logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.VERIFY_PROMPT_DESKTOP, query,
                    logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get verification response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_verify_failed");
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
                        pageCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.GREEN);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_verify_passed");
            } else {
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_verify_result");
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
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to verify using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
