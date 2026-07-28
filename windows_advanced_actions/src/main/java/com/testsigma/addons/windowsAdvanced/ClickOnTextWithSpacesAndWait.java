package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Action(actionText = "Click on the sentence sentence-to-click with maximum wait time wait-time-in-seconds seconds",
        description = "This action waits for the specified text to appear on the screen and then clicks on it. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Click on sentence with wait",
        useCustomScreenshot = true)
public class ClickOnTextWithSpacesAndWait extends WindowsAdvancedAction {

    @TestData(reference = "sentence-to-click")
    private com.testsigma.sdk.TestData textToClick;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData maxWaitSeconds;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    ObjectMapper mapper = new ObjectMapper();
    OCRResponse ocrResponse = new OCRResponse();

    private static final int POLLING_INTERVAL_MS = 1500;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Click On Text With Wait: Starting Execution ===");

        try {
            String targetText = textToClick.getValue().toString();
            // Convert seconds to milliseconds
            int timeoutMs = Integer.parseInt(maxWaitSeconds.getValue().toString()) * 1000;

            logger.info("Looking for text to click: '" + targetText + "' with max wait time: "
                    + maxWaitSeconds.getValue() + " seconds");

            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;
            OCRUtils ocrUtils = new OCRUtils();
            Robot robot = new Robot();
            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - checking for text: '" + targetText + "'");

                // Capture the current screen
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x"
                        + screenCapture.getHeight());

                // Save the screenshot to a temporary file
                File screenshotFile = saveScreenshotToFile(screenCapture, "click_text_screenshot");

                // Extract text points using OCR
                List<OCRTextPoint> textPoints = extractTextPoints(screenshotFile);
                logger.info("Found " + textPoints.size() + " text elements");

                // Find the matching text
                OCRTextPoint targetTextPoint = ocrUtils.findMatchingTextForSentence(textPoints, targetText, logger);
                if (targetTextPoint != null) {
                    // Text found - perform click and return success
                    logger.info("Found Text point with text = " + targetTextPoint.getText()
                            + ", x1 = " + targetTextPoint.getX1() + ", y1 = " + targetTextPoint.getY1() +
                            ", x2 = " + targetTextPoint.getX2() + ", y2 = " + targetTextPoint.getY2());

                    int clickX = (int) targetTextPoint.getCenterX();
                    int clickY = (int) targetTextPoint.getCenterY();
                    logger.info("Clicking on text at coordinates: (" + clickX + ", " + clickY + ")");

                    performClickWithRobot(robot, clickX, clickY);
                    logger.info("Successfully clicked on text: '" + targetText +
                            "' at coordinates (" + clickX + ", " + clickY + ")");

                    setSuccessMessage(String.format(
                            "Successfully clicked on text: <b>%s</b> at coordinates: x-<b>%d</b>, y-<b>%d</b>",
                            targetText, clickX, clickY
                    ));
                    // wait for one second before taking screenshot
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        // Ignore
                    }
                    // Upload final screenshot to S3
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);
                    return Result.SUCCESS;
                }

                // Text not found - check if we should continue polling
                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > POLLING_INTERVAL_MS) {
                    logger.info("Text not found yet. Waiting " + (POLLING_INTERVAL_MS / 1000.0)
                            + " seconds before next attempt. " +
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
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult,
                    "click_text_wait_failure_screenshot", logger);
            return Result.FAILED;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult,
                    "click_text_wait_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    // i have not moved these three methods to utility class as i am facing some issue with files being passed as
    // argument to utility classes.
    /**
     * Extracts text points from the screenshot using OCR API
     */
    private List<OCRTextPoint> extractTextPoints(File screenshotFile) throws Exception {
        logger.info("Extracting text points from screenshot: " + screenshotFile.getAbsolutePath());
        // Delegate to the shared OCRUtils implementation, which handles connection
        // closing (Connection: close), proxy/direct fallback, retries and response cleanup.
        return OCRUtils.extractTextPoints(screenshotFile, logger);
    }

    /**
     * Performs click using Robot with appropriate delays
     */
    private void performClickWithRobot(Robot robot, int x, int y) throws Exception {
        // Move mouse to the target location
        logger.info("Moving mouse to coordinates (" + x + ", " + y + ")");
        robot.mouseMove(x, y);
        Thread.sleep(200); // Delay to ensure mouse is positioned

        // Press mouse button
        logger.info("Pressing mouse button");
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(100); // Delay between press and release

        // Release mouse button
        logger.info("Releasing mouse button");
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(200); // Delay after click completion

        logger.info("Click completed successfully");
    }


    /**
     * Saves a BufferedImage to a temporary file
     */
    public static File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            System.out.println("Screenshot saved to: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            System.err.println("Error saving screenshot to file: " + e.getMessage());
            throw new Exception("Failed to save screenshot: " + e.getMessage());
        }
    }

}
