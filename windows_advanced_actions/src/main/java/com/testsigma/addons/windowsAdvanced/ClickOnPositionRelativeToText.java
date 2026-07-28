package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.Constants;
import com.testsigma.addons.util.OCRResponse;
import com.testsigma.addons.util.OCRUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.addons.util.OCRTextPoint;
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

@Action(actionText = "Click on the position-type relative to the text text-to-find with pixel offset pixel-offset" +
        " and maximum wait time wait-time-in-seconds seconds",
        description = "This action finds the specified text on the screen and clicks at a position relative to it with a pixel offset. " +
                "Position can be Left, Right, Top, Bottom, or Center of the text. " +
                "The pixel offset determines how far from the text edge to click (positive values move away from text, negative values move towards text). " +
                "For Center position, offset is ignored. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Click on position relative to text",
        useCustomScreenshot = true)
public class ClickOnPositionRelativeToText extends WindowsAdvancedAction {

    @TestData(reference = "text-to-find")
    private com.testsigma.sdk.TestData textToFind;

    @TestData(reference = "position-type", allowedValues = {"Left", "Right", "Top", "Bottom", "Center"})
    private com.testsigma.sdk.TestData position;

    @TestData(reference = "pixel-offset")
    private com.testsigma.sdk.TestData pixelOffset;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData maxWaitSeconds;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    ObjectMapper mapper = new ObjectMapper();
    OCRResponse ocrResponse = new OCRResponse();

    private static final int POLLING_INTERVAL_MS = 1500; // 1.5 second polling interval

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Click On Position Relative To Text: Starting Execution ===");

        try {
            String targetText = textToFind.getValue().toString();
            String positionValue = position.getValue().toString();
            int offset = Integer.parseInt(pixelOffset.getValue().toString());
            int timeoutMs = Integer.parseInt(maxWaitSeconds.getValue().toString()) * 1000; // Convert seconds to milliseconds

            logger.info("Looking for text: '" + targetText + "' to click " + positionValue +
                    " with offset: " + offset + " pixels, max wait time: " + maxWaitSeconds.getValue() + " seconds");

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
                File screenshotFile = saveScreenshotToFile(screenCapture, "click_relative_position_screenshot");

                // Extract text points using OCR
                List<OCRTextPoint> textPoints = extractTextPoints(screenshotFile);
                logger.info("Found " + textPoints.size() + " text elements");

                // Find the matching text
                OCRTextPoint textPoint = findMatchingText(textPoints, targetText);

                if (textPoint != null) {
                    logger.info("Found text with coordinates: x1=" + textPoint.getX1() + ", y1=" + textPoint.getY1() +
                            ", x2=" + textPoint.getX2() + ", y2=" + textPoint.getY2());

                    // Calculate click position based on position and offset
                    Point clickPoint = calculateClickPosition(textPoint, positionValue, offset);
                    logger.info("Calculated click position: (" + clickPoint.x + ", " + clickPoint.y + ")");

                    // Perform the click
                    performClickWithRobot(clickPoint.x, clickPoint.y);

                    logger.info("Successfully clicked " + positionValue + " of text '" + targetText +
                            "' with offset " + offset + " pixels at coordinates (" +
                            clickPoint.x + ", " + clickPoint.y + ")");
                    setSuccessMessage("Successfully clicked " + positionValue + " of text '" + targetText +
                            "' with offset " + offset + " pixels at coordinates (" +
                            clickPoint.x + ", " + clickPoint.y + ")");

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
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_relative_position_failure_screenshot", logger);
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid numeric value: " + e.getMessage());
            setErrorMessage("Invalid numeric value provided. Please check timeout and pixel offset values.");
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_relative_position_failure_screenshot", logger);
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_relative_position_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    /**
     * Calculates the click position based on text point, position, and offset
     * @param textPoint The OCR text point
     * @param position The relative position (Left, Right, Top, Bottom, Center)
     * @param offset The pixel offset from the text
     * @return Point with calculated click coordinates
     */
    private Point calculateClickPosition(OCRTextPoint textPoint, String position, int offset) {
        int centerX = (int) ((textPoint.getX1() + textPoint.getX2()) / 2);
        int centerY = (int) ((textPoint.getY1() + textPoint.getY2()) / 2);

        int clickX = centerX;
        int clickY = centerY;

        switch (position.toUpperCase()) {
            case "LEFT":
                clickX = (int) textPoint.getX1() - offset;
                clickY = centerY;
                break;
            case "RIGHT":
                clickX = (int) textPoint.getX2() + offset;
                clickY = centerY;
                break;
            case "TOP":
                clickX = centerX;
                clickY = (int) textPoint.getY1() - offset;
                break;
            case "BOTTOM":
                clickX = centerX;
                clickY = (int) textPoint.getY2() + offset;
                break;
            case "CENTER":
                // For center, offset is ignored
                clickX = centerX;
                clickY = centerY;
                break;
            default:
                logger.debug("Unknown position: " + position + ". Using center.");
                break;
        }

        return new Point(clickX, clickY);
    }

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
     * Finds the matching text in the list of text points
     */
    private OCRTextPoint findMatchingText(List<OCRTextPoint> textPoints, String targetText) {
        logger.info("Searching for text: '" + targetText + "'");

        // First try exact match
        for (OCRTextPoint textPoint : textPoints) {
            if (textPoint.getText().equals(targetText)) {
                logger.info("Found exact match: " + textPoint.getText());
                return textPoint;
            }
        }

        // Then try case-insensitive match
        for (OCRTextPoint textPoint : textPoints) {
            if (textPoint.getText().equalsIgnoreCase(targetText)) {
                logger.info("Found case-insensitive match: " + textPoint.getText());
                return textPoint;
            }
        }

        // Finally try contains match
        for (OCRTextPoint textPoint : textPoints) {
            if (textPoint.getText().toLowerCase().contains(targetText.toLowerCase())) {
                logger.info("Found contains match: " + textPoint.getText());
                return textPoint;
            }
        }

        logger.warn("No matching text found for: '" + targetText + "'");
        logger.info("Available text elements:");
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("  - '" + textPoint.getText() + "'");
        }

        return null;
    }

    /**
     * Performs click using Robot with appropriate delays
     */
    private void performClickWithRobot(int x, int y) throws Exception {
        Robot robot = new Robot();

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
