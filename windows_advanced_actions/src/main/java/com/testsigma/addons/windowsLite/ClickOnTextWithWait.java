package com.testsigma.addons.windowsLite;


import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import com.testsigma.sdk.ApplicationType;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.NoSuchElementException;
import java.util.List;

@Action(actionText = "Click on text text-to-click with maximum wait time wait-time-in-seconds seconds",
        description = "This action waits for the specified text to appear on the screen and then clicks on it. " +
                "it  performs a mouse click at the center of the text area. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_UFT,
        displayName = "Click on text with wait")
public class ClickOnTextWithWait extends WindowsAction {

    @TestData(reference = "text-to-click")
    private com.testsigma.sdk.TestData textToClick;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData maxWaitSeconds;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

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

                OCRImage ocrImage = new OCRImage();
                ocrImage.setOcrImageFile(screenshotFile);
                List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
                printAllCoordinates(textPoints);
                OCRTextPoint textPoint = getTextPointFromText(textPoints);
                if (textPoint != null) {
                    // Text found - perform click and return success
                    logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                            ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                    int x = (textPoint.getX1() + textPoint.getX2()) / 2;
                    int y = (textPoint.getY1() + textPoint.getY2()) / 2;
                    robot.mouseMove(x, y);
                    Thread.sleep(100); // Small delay to ensure mouse is positioned
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(50);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(1000); // Wait a moment after click
                    logger.info("Successfully clicked on text: '" + targetText + "' at coordinates (" + x + ", " + y + ")");
                    setSuccessMessage("Successfully clicked on text '" + targetText + "' at coordinates (" + x + ", " + y + ")");
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
            return Result.FAILED;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
            // Capture and upload screenshot even on failure
            return Result.FAILED;
        }
    }


    /**
     * Saves the screenshot to a temporary file
     *
     * @param screenshot The captured screenshot
     * @param fileName   The base filename
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

    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints) {
        if (textPoints == null) {
            return null;
        }
        for (OCRTextPoint textPoint : textPoints) {
            if (textToClick.getValue().equals(textPoint.getText())) {
                return textPoint;

            }
        }
        return null;
    }

    private void printAllCoordinates(List<OCRTextPoint> textPoints) {
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("text =" + textPoint.getText() + "x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() + "\n\n\n\n");
        }
    }
}
