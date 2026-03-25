package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.Constants;
import com.testsigma.addons.util.OCRResponse;
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

@Action(actionText = "Click on text text-to-click with maximum wait time wait-time-in-seconds seconds",
        description = "This action waits for the specified text to appear on the screen and then clicks on it. " +
                "It uses OCR to locate the text within the screen and performs a mouse click at the center of the text area. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Click on text with wait",
        useCustomScreenshot = true)
public class ClickOnTextWithWait extends WindowsAdvancedAction {

    @TestData(reference = "text-to-click")
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

                // Extract text points using OCR
                List<OCRTextPoint> textPoints = extractTextPoints(screenshotFile);
                logger.info("Found " + textPoints.size() + " text elements");

                // Find the matching text
                OCRTextPoint targetTextPoint = findMatchingText(textPoints, targetText);
                if (targetTextPoint != null) {
                    // Text found - perform click and return success
                    logger.info("Found Textpoint with text = " + targetTextPoint.getText() + ", x1 = " + targetTextPoint.getX1() +
                            ", y1 = " + targetTextPoint.getY1() + ", x2 = " + targetTextPoint.getX2() + ", y2 = " + targetTextPoint.getY2());

                    int clickX = (int) targetTextPoint.getCenterX();
                    int clickY = (int) targetTextPoint.getCenterY();
                    logger.info("Clicking on text at coordinates: (" + clickX + ", " + clickY + ")");

                    performClickWithRobot(clickX, clickY);
                    logger.info("Successfully clicked on text: '" + targetText + "' at coordinates (" + clickX + ", " + clickY + ")");

                    setSuccessMessage(String.format(
                            "Successfully clicked on text: <b>%s</b> at coordinates: x-<b>%d</b>, y-<b>%d</b>",
                            targetText, clickX, clickY
                    ));

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
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_text_wait_failure_screenshot", logger);
            return Result.FAILED;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_text_wait_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    /**
     * Extracts text points from the screenshot using OCR API
     */
    private List<OCRTextPoint> extractTextPoints(File screenshotFile) throws Exception {
        try {
            logger.info("Extracting text points from screenshot: " + screenshotFile.getAbsolutePath());
            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("ocrImageFile", screenshotFile.getName(),
                            RequestBody.create(screenshotFile, MediaType.parse("image/png")))
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_OCR_TEXT_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();

            logger.info("Making OCR API call");
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                logger.info("OCR API response is successful");
                if (response.body() != null) {
                    String responseBody = response.body().string();
                    logger.info("OCR Response body: " + responseBody);

                    ocrResponse = mapper.readValue(responseBody, OCRResponse.class);
                    logger.info("Deserialized OCR response");

                    if (ocrResponse.hasError()) {
                        throw new RuntimeException("OCR API returned error: " + ocrResponse.getError());
                    }

                    if (!ocrResponse.hasText()) {
                        throw new RuntimeException("No text found in the image");
                    }

                    return ocrResponse.getText();
                } else {
                    throw new RuntimeException("OCR API returned null response body");
                }
            } else {
                throw new RuntimeException("OCR API call failed with status: " + response.code());
            }

        } catch (IOException e) {
            logger.info("Exception during OCR API call: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Error during OCR API call: " + e.getMessage());
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
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
