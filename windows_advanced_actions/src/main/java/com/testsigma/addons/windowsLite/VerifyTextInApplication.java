package com.testsigma.addons.windowsLite;

import com.testsigma.addons.util.AiTextUtils;
import com.testsigma.addons.util.ScreenshotUtils;


import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.NoSuchElementException;

@Data
@Action(actionText = "AI: verify that the text text-to-verify is present in opened application and store result in runtime variable test-data1",
        description = "This action verifies that the specified text is present in the opened application" +
                " using AI capabilities. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS,
        displayName = "Verify if text is present in application",
        useCustomScreenshot = true)
public class VerifyTextInApplication extends WindowsAction {

    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "test-data1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData1;

    @AI
    private com.testsigma.sdk.AI ai;
    
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private final String prompt = "You are provided with a screenshot of a computer application." +
            " Your task is to analyze this screenshot and determine if the specified text is present anywhere in " +
            "the image.Look for the text in any form - it could be in buttons, labels, text fields, menus, " +
            "or any other UI element. Return only 'YES' if the text is found, or 'NO' if the text is not found. " +
            "The text to search for is: ";

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== AI Text Verification: Starting Execution ===");

        try {
            String expectedText = testData.getValue().toString();
            logger.info("Looking for text: " + expectedText);

            // Capture the current screen
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            // Save the screenshot to a temporary file
            File screenshotFile = saveScreenshotToFile(screenCapture, "application_screenshot");
            logger.info("Screenshot saved to: " + screenshotFile.getAbsolutePath());

            String fullPrompt = prompt + "'" + expectedText + "'. ";
            logger.info("Sending AI prompt: " + fullPrompt);

            // Invoke AI (primary model with automatic fallback on an empty response)
            String aiResponse = AiTextUtils.invokeAiWithFallback(ai, screenshotFile, fullPrompt, logger);

            // Parse AI response
            boolean textFound = parseAIResponse(aiResponse);
            
            // Upload screenshot to S3
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);
            
            if (textFound) {
                logger.info("Text found in application. Step passed.");
                setSuccessMessage("Text '" + expectedText + "' was found in the application.");
                return Result.SUCCESS;
            } else {
                logger.debug("Text not found in application. Step failed.");
                setErrorMessage("Text '" + expectedText + "' was not found in the application.");
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.debug("Exception during AI text verification: " + e.getMessage());
            setErrorMessage("Error during text verification: " + e.getMessage());
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "verify_text_failure_screenshot", logger);
            return Result.FAILED;
        }
    }



    /**
     * Saves the screenshot to a temporary file
     * @param screenshot The captured screenshot
     * @param fileName The base filename
     * @return The temporary file
     * @throws Exception if file creation fails
     */
    private File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            logger.info("Screenshot saved to temporary file: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            logger.debug("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for AI processing.", e);
        }
    }

    /**
     * Parses the AI response to determine if text was found
     * @param aiResponse The response from AI
     * @return true if text was found, false otherwise
     */
    private boolean parseAIResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            logger.debug("AI response is null or empty");
            return false;
        }

        String response = aiResponse.trim().toUpperCase();
        logger.info("Parsing AI response: " + response);

        // Check for various positive responses
        if (response.contains("YES") || response.contains("TRUE") || response.contains("FOUND") || 
            response.contains("PRESENT") || response.contains("EXISTS")) {
            return true;
        }

        // Check for various negative responses
        if (response.contains("NO") || response.contains("FALSE") || response.contains("NOT FOUND") || 
            response.contains("ABSENT") || response.contains("NOT PRESENT")) {
            return false;
        }

        // If response is unclear, log it and return false
        logger.debug("Unclear AI response: " + aiResponse + ". Treating as 'not found'.");
        return false;
    }
} 