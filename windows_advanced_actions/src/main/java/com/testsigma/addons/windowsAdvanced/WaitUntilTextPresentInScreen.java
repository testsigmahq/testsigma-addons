package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.OCRTextPoint;
import com.testsigma.addons.util.OCRUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Action(actionText = "Wait until text text-to-verify is present in screen with timeout wait-time-in-seconds seconds",
        description = "This action waits until the specified text is present on the screen using OCR API capabilities. " +
                "It polls every 1.5 seconds until the text is found or timeout is reached. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Wait until the text is present on the screen",
        useCustomScreenshot = true)
public class WaitUntilTextPresentInScreen extends WindowsAdvancedAction {

    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData textToSearch;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData timeoutSeconds;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int POLLING_INTERVAL_MS = 1500;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Wait Until Text Present (OCR): Starting Execution ===");

        try {
            String expectedText = textToSearch.getValue().toString();
            int timeoutMs = Integer.parseInt(timeoutSeconds.getValue().toString()) * 1000;

            logger.info("Looking for text: '" + expectedText + "' with timeout: " +
                    timeoutSeconds.getValue() + " seconds");

            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;

            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - checking for text: '" + expectedText + "'");

                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);

                File screenshotFile = saveScreenshotToFile(screenCapture, "wait_text_screenshot");

                List<OCRTextPoint> textPoints = OCRUtils.extractTextPoints(screenshotFile, logger);
                logger.info("Found " + textPoints.size() + " text elements via OCR");

                boolean textFound = OCRUtils.searchForText(textPoints, expectedText, logger);

                if (textFound) {
                    logger.info("Text found in application. Wait successful.");
                    setSuccessMessage("Text '" + expectedText + "' was found on the screen after waiting.");
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);
                    return Result.SUCCESS;
                }

                if (screenshotFile.exists()) {
                    screenshotFile.delete();
                }

                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > POLLING_INTERVAL_MS) {
                    logger.info("Text not found yet. Waiting " + (POLLING_INTERVAL_MS / 1000)
                            + " second before next attempt. " +
                            "Remaining time: " + (remainingTime / 1000) + " seconds");
                    Thread.sleep(POLLING_INTERVAL_MS);
                } else {
                    break;
                }
            }

            logger.debug("Timeout reached. Text '" + expectedText + "' was not found on the screen within " +
                    timeoutSeconds.getValue() + " seconds.");
            setErrorMessage("Text '" + expectedText + "' was not found on the screen within " +
                    timeoutSeconds.getValue() + " seconds.");
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_text_failure_screenshot", logger);
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid timeout value: " + timeoutSeconds.getValue());
            setErrorMessage("Invalid timeout value: " + timeoutSeconds.getValue() +
                    ". Please provide a valid number of seconds.");
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_text_failure_screenshot", logger);
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during wait operation: " + e.getMessage());
            setErrorMessage("Error during wait operation: " + e.getMessage());
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_text_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    private File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            return tempFile;
        } catch (Exception e) {
            logger.debug("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for processing.", e);
        }
    }
}
