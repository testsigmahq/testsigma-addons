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
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Action(actionText = "Click on text test-data with maximum wait time test-data2 seconds",
        description = "This action waits for the specified text to appear on the screen and then clicks on it. " +
                "It uses AI to locate the text and performs a mouse click at the center of the text area. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "ClickOnTextWithWait",
        useCustomScreenshot = true)
public class ClickOnTextWithWait extends WindowsAdvancedAction {

    @TestData(reference = "test-data", description = "The text to search for and click on")
    private com.testsigma.sdk.TestData textToClick;

    @TestData(reference = "test-data2", description = "Maximum wait time in seconds")
    private com.testsigma.sdk.TestData maxWaitSeconds;

    @AI
    private com.testsigma.sdk.AI ai;
    
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;
    
    private final String prompt = "You are provided with a screenshot of a computer application with dimensions " +
            "WIDTHxHEIGHT pixels. Your task is to analyze this screenshot and determine if the specified text is present anywhere in " +
            "the image. If the text is found, you must also provide the coordinates (x,y) of the center of the text area, " +
            "where x and y are pixel coordinates within the image dimensions (0,0 is top-left corner). " +
            "Look for the text in any form - it could be in buttons, labels, text fields, menus, " +
            "or any other UI element. " +
            "Return 'YES,x,y' if the text is found (where x,y are the pixel coordinates), or 'NO' if the text is not found. " +
            "The text to search for is: ";

    private static final int POLLING_INTERVAL_MS = 1500; // 1.5 second polling interval

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Click On Text With Wait: Starting Execution ===");

        try {
            String targetText = textToClick.getValue().toString();
            int timeoutMs = Integer.parseInt(maxWaitSeconds.getValue().toString()) * 1000; // Convert seconds to milliseconds
            
            logger.info("Looking for text to click: '" + targetText + "' with max wait time: " + maxWaitSeconds.getValue() + " seconds");

            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;
            
            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - checking for text: '" + targetText + "'");
                
                // Capture the current screen
                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
                
                // Save the screenshot to a temporary file
                File screenshotFile = saveScreenshotToFile(screenCapture, "click_text_screenshot");
                
                // Create AI request with screen dimensions
                AIRequest aiRequest = new AIRequest();
                String fullPrompt = prompt.replace("WIDTHxHEIGHT", 
                        screenCapture.getWidth() + "x" + screenCapture.getHeight()) + 
                        "'" + targetText + "'. ";
                aiRequest.setPrompt(fullPrompt);
                aiRequest.setModel("gpt-4o");

                // Add the screenshot file
                ArrayList<File> files = new ArrayList<>();
                files.add(screenshotFile);
                aiRequest.setFiles(files);

                // Invoke AI
                String aiResponse = ai.invokeAI(aiRequest);
                logger.info("AI response: " + aiResponse);

                // Parse AI response for text location
                ClickLocation clickLocation = parseAIResponseForClick(aiResponse);
                
                if (clickLocation != null && clickLocation.isFound()) {
                    logger.info("Text found at coordinates: (" + clickLocation.getX() + ", " + clickLocation.getY() + ")");
                    
                    // Perform the click
                    robot.mouseMove(clickLocation.getX(), clickLocation.getY());
                    Thread.sleep(100); // Small delay to ensure mouse is positioned
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(50);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    
                    logger.info("Successfully clicked on text: '" + targetText + "' at coordinates (" + 
                            clickLocation.getX() + ", " + clickLocation.getY() + ")");
                    setSuccessMessage("Successfully clicked on text '" + targetText + "' at coordinates (" + 
                            clickLocation.getX() + ", " + clickLocation.getY() + ")");
                    
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
            logger.debug("Timeout reached. Text '" + targetText + "' was not found on the screen within " + 
                    maxWaitSeconds.getValue() + " seconds.");
            setErrorMessage("Text '" + targetText + "' was not found on the screen within " + 
                    maxWaitSeconds.getValue() + " seconds. Unable to perform click.");
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_text_wait_failure_screenshot", logger);
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid timeout value: " + maxWaitSeconds.getValue());
            setErrorMessage("Invalid timeout value: " + maxWaitSeconds.getValue() +
                    ". Please provide a valid number of seconds.");
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_text_wait_failure_screenshot", logger);
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_text_wait_failure_screenshot", logger);
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
     * Parses the AI response to determine if text was found and get click coordinates
     * @param aiResponse The response from AI
     * @return ClickLocation object with coordinates if found, null otherwise
     */
    private ClickLocation parseAIResponseForClick(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            logger.debug("AI response is null or empty");
            return null;
        }

        String response = aiResponse.trim().toUpperCase();
        logger.debug("Parsing AI response for click: " + response);

        // Check for positive response with coordinates (format: YES,x,y)
        if (response.startsWith("YES,")) {
            try {
                String[] parts = response.split(",");
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[1].trim());
                    int y = Integer.parseInt(parts[2].trim());
                    logger.info("Parsed coordinates: x=" + x + ", y=" + y);
                    return new ClickLocation(x, y, true);
                }
            } catch (NumberFormatException e) {
                logger.debug("Failed to parse coordinates from AI response: " + aiResponse);
            }
        }

        // Check for various negative responses
        if (response.contains("NO") || response.contains("FALSE") || response.contains("NOT FOUND") || 
            response.contains("ABSENT") || response.contains("NOT PRESENT")) {
            return new ClickLocation(0, 0, false);
        }

        // If response is unclear, log it and return null
        logger.debug("Unclear AI response: " + aiResponse + ". Treating as 'not found'.");
        return null;
    }

    /**
     * Inner class to hold click location information
     */
    private static class ClickLocation {
        private final int x;
        private final int y;
        private final boolean found;

        public ClickLocation(int x, int y, boolean found) {
            this.x = x;
            this.y = y;
            this.found = found;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public boolean isFound() { return found; }
    }
}
