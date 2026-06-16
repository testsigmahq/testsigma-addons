package com.testsigma.addons.salesforce;

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
@Action(actionText = "Ai: store the output from prompt ai-prompt into a runtime variable runtime-variable",
        description = "uses AI prompt to extract specific " +
                "content or values visible on the page, and store the result into a runtime variable. " +
                "Use natural language to describe what content to extract (e.g. 'the order number', 'the total price').",
        displayName = "Ai: Store output from AI prompt",
        applicationType = ApplicationType.Salesforce,
        useCustomScreenshot = true)
public class StoreOutputFromAiPrompt extends SalesforceAction {

    @TestData(reference = "ai-prompt")
    private com.testsigma.sdk.TestData aiPrompt;

    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @com.testsigma.sdk.annotation.RunTimeData
    private RunTimeData runTimeData;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        logger.info("=== StoreOutputFromAiPrompt (Salesforce): Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {
            String prompt       = aiPrompt.getValue().toString();
            String variableName = runtimeVariable.getValue().toString();
            logger.info("AI prompt: " + prompt + " | target variable: " + variableName);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Viewport screenshot size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_salesforce_store_capture", logger);

            String aiResponse = AiActionUtils.invokeAi(ai, screenshotFile, AiActionUtils.STORE_PROMPT_SALESFORCE, prompt,
                    logger);

            JsonNode responseNode = AiActionUtils.parseAiJson(aiResponse, logger);
            if (responseNode == null) {
                setErrorMessage("Failed to get extraction response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_store_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            boolean found      = responseNode.path("found").asBoolean(false);
            String output      = responseNode.path("output").asText("");
            int aiX1           = responseNode.path("x1").asInt(0);
            int aiY1           = responseNode.path("y1").asInt(0);
            int aiX2           = responseNode.path("x2").asInt(0);
            int aiY2           = responseNode.path("y2").asInt(0);
            int imageWidth     = responseNode.path("imageWidth").asInt(0);
            int imageHeight    = responseNode.path("imageHeight").asInt(0);
            int confidence     = responseNode.path("confidence").asInt(0);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI extraction result — found=%b | output='%s' | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    found, output, imageWidth, imageHeight, confidence, description));

            if (found && imageWidth > 0 && imageHeight > 0 && (aiX1 | aiY1 | aiX2 | aiY2) != 0) {
                int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
                int capCX = (cap[0] + cap[2]) / 2;
                int capCY = (cap[1] + cap[3]) / 2;
                BufferedImage annotated = AiActionUtils.drawHighlight(
                        pageCapture, cap[0], cap[1], cap[2], cap[3], capCX, capCY, Color.GREEN);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_store_passed");
            } else {
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_store_result");
            }
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            if (found) {
                runTimeData.setKey(variableName);
                runTimeData.setValue(output);
                setSuccessMessage(String.format(
                        "Stored '%s' into variable '%s' | confidence=%d | %s",
                        output, variableName, confidence, description));
                return Result.SUCCESS;
            } else {
                setErrorMessage(String.format(
                        "Could not extract output for prompt '%s' | confidence=%d | %s",
                        prompt, confidence, description));
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to store AI output. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
