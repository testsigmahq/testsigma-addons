package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.windowsAdvanced.utils.ScreenshotUtils;
import com.testsigma.sdk.AIRequest;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Data
@Action(actionText = "Wait until text test-data is present in screen with timeout test-data2 seconds",
        description = "This action waits until the specified text is present on the screen using AI capabilities. " +
                "It polls every 1 second until the text is found or timeout is reached. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "WaitUntilTextPresentInScreen",
        useCustomScreenshot = true)
public class WaitUntilTextPresentInScreen extends WindowsAdvancedAction {

    @TestData(reference = "test-data", description = "The text to search for on the screen")
    private com.testsigma.sdk.TestData textToSearch;

    @TestData(reference = "test-data2", description = "Timeout in seconds")
    private com.testsigma.sdk.TestData timeoutSeconds;

    @AI
    private com.testsigma.sdk.AI ai;
    
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;
    
    private final String prompt = "You are provided with a screenshot of a computer application." +
            " Your task is to analyze this screenshot and determine if the specified text is present anywhere in " +
            "the image. Look for the text in any form - it could be in buttons, labels, text fields, menus, " +
            "or any other UI element. Return only 'YES' if the text is found, or 'NO' if the text is not found. " +
            "The text to search for is: ";

    private static final int POLLING_INTERVAL_MS = 1500; // 1 second polling interval

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Wait Until Text Present: Starting Execution ===");

        try {
            String expectedText = textToSearch.getValue().toString();
            int timeoutMs = Integer.parseInt(timeoutSeconds.getValue().toString()) * 1000;
            
            logger.info("Looking for text: '" + expectedText + "' with timeout: " +
                    timeoutSeconds.getValue() + " seconds");

            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;
            
            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - checking for text: '" + expectedText + "'");
                
                // Capture the current screen
                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                
                // Save the screenshot to a temporary file
                File screenshotFile = saveScreenshotToFile(screenCapture, "wait_text_screenshot");
                
                // Create AI request
                AIRequest aiRequest = new AIRequest();
                String fullPrompt = prompt + "'" + expectedText + "'. ";
                aiRequest.setPrompt(fullPrompt);
                aiRequest.setModel("gpt-4o");

                // Add the screenshot file
                ArrayList<File> files = new ArrayList<>();
                files.add(screenshotFile);
                aiRequest.setFiles(files);

                // Invoke AI
                String aiResponse = ai.invokeAI(aiRequest);
                logger.info("AI response: " + aiResponse);

                // Parse AI response
                boolean textFound = parseAIResponse(aiResponse);
                
                if (textFound) {
                    logger.info("Text found in application. Wait successful.");
                    setSuccessMessage("Text '" + expectedText + "' was found on the screen after waiting.");
                    
                    // Upload final screenshot to S3
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);
                    
                    return Result.SUCCESS;
                }
                
                // Clean up temporary file
                if (screenshotFile.exists()) {
                    screenshotFile.delete();
                }
                
                // Check if we should continue polling
                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > POLLING_INTERVAL_MS) {
                    logger.info("Text not found yet. Waiting " + (POLLING_INTERVAL_MS / 1000)
                            + " second before next attempt. " +
                            "Remaining time: " + (remainingTime / 1000) + " seconds");
                    Thread.sleep(POLLING_INTERVAL_MS);
                } else {
                    break; // No time left for another attempt
                }
            }
            
            // If we reach here, timeout occurred
            logger.debug("Timeout reached. Text '" + expectedText + "' was not found on the screen within " + 
                    timeoutSeconds.getValue() + " seconds.");
            setErrorMessage("Text '" + expectedText + "' was not found on the screen within " + 
                    timeoutSeconds.getValue() + " seconds.");
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_text_failure_screenshot", logger);
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid timeout value: " + timeoutSeconds.getValue());
            setErrorMessage("Invalid timeout value: " + timeoutSeconds.getValue() +
                    ". Please provide a valid number of seconds.");
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_text_failure_screenshot", logger);
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during wait operation: " + e.getMessage());
            setErrorMessage("Error during wait operation: " + e.getMessage());
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_text_failure_screenshot", logger);
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
        logger.debug("Parsing AI response: " + response);

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
