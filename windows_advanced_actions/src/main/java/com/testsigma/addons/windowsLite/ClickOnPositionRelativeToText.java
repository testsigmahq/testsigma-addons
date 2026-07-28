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

@Action(actionText = "lite: Click on position position-type relative to the text text-to-find with pixel offset pixel-offset and maximum wait time wait-time-in-seconds seconds",
        description = "This action finds the specified text on the screen and clicks at a position relative to it with a pixel offset. " +
                "Position can be Left, Right, Top, Bottom, or Center of the text. " +
                "The pixel offset determines how far from the text edge to click (positive values move away from text, negative values move towards text). " +
                "For Center position, offset is ignored. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_UFT,
        displayName = "Click on position relative to text")
public class ClickOnPositionRelativeToText extends WindowsAction {

    @TestData(reference = "text-to-find")
    private com.testsigma.sdk.TestData textToFind;

    @TestData(reference = "position-type", allowedValues = {"Left", "Right", "Top", "Bottom", "Center"})
    private com.testsigma.sdk.TestData position;

    @TestData(reference = "pixel-offset")
    private com.testsigma.sdk.TestData pixelOffset;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData maxWaitSeconds;

    @OCR
    private com.testsigma.sdk.OCR ocr;
    
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Click On Position Relative To Text: Starting Execution ===");

        try {
            String targetText = textToFind.getValue().toString();
            String positionValue = position.getValue().toString();
            int offset = Integer.parseInt(pixelOffset.getValue().toString());
            
            logger.info("Looking for text: '" + targetText + "' to click " + positionValue + 
                    " with offset: " + offset + " pixels, max wait time: " + maxWaitSeconds.getValue() + " seconds");

                logger.info("Polling attempt - checking for text: '" + targetText + "'");
                
                // Capture the current screen
                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
                
                // Save the screenshot to a temporary file
                File screenshotFile = saveScreenshotToFile(screenCapture, "click_relative_position_screenshot");
                
                // Use OCR to find text
                OCRImage ocrImage = new OCRImage();
                ocrImage.setOcrImageFile(screenshotFile);
                List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
                printAllCoordinates(textPoints);
                OCRTextPoint textPoint = getTextPointFromText(textPoints, targetText);
                
                if (textPoint != null) {
                    logger.info("Found text with coordinates: x1=" + textPoint.getX1() + ", y1=" + textPoint.getY1() + 
                            ", x2=" + textPoint.getX2() + ", y2=" + textPoint.getY2());
                    
                    // Calculate click position based on position and offset
                    Point clickPoint = calculateClickPosition(textPoint, positionValue, offset);
                    logger.info("Calculated click position: (" + clickPoint.x + ", " + clickPoint.y + ")");
                    
                    // Perform the click
                    robot.mouseMove(clickPoint.x, clickPoint.y);
                    Thread.sleep(100); // Small delay to ensure mouse is positioned
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(50);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    
                    logger.info("Successfully clicked " + positionValue + " of text '" + targetText + 
                            "' with offset " + offset + " pixels at coordinates (" + 
                            clickPoint.x + ", " + clickPoint.y + ")");
                    setSuccessMessage("Successfully clicked " + positionValue + " of text '" + targetText + 
                            "' with offset " + offset + " pixels at coordinates (" + 
                            clickPoint.x + ", " + clickPoint.y + ")");
                    
                    // Clean up temporary file
                    if (screenshotFile.exists()) {
                        screenshotFile.delete();
                    }
                    
                    return Result.SUCCESS;
                }
                
                // Clean up temporary file
                if (screenshotFile.exists()) {
                    screenshotFile.delete();
                }
                
                
            
            // If we reach here, timeout occurred
            logger.debug("Timeout reached. Text '" + targetText + "' was not found on the screen within " + 
                    maxWaitSeconds.getValue() + " seconds.");
            setErrorMessage("Text '" + targetText + "' was not found on the screen within " + 
                    maxWaitSeconds.getValue() + " seconds. Unable to perform click.");
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid numeric value: " + e.getMessage());
            setErrorMessage("Invalid numeric value provided. Please check timeout and pixel offset values.");
            return Result.FAILED;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
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
        int centerX = (textPoint.getX1() + textPoint.getX2()) / 2;
        int centerY = (textPoint.getY1() + textPoint.getY2()) / 2;
        
        int clickX = centerX;
        int clickY = centerY;
        
        switch (position.toUpperCase()) {
            case "LEFT":
                clickX = textPoint.getX1() - offset;
                clickY = centerY;
                break;
            case "RIGHT":
                clickX = textPoint.getX2() + offset;
                clickY = centerY;
                break;
            case "TOP":
                clickX = centerX;
                clickY = textPoint.getY1() - offset;
                break;
            case "BOTTOM":
                clickX = centerX;
                clickY = textPoint.getY2() + offset;
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
     * Gets the OCRTextPoint for the target text
     * @param textPoints List of OCR text points
     * @param targetText The text to find
     * @return OCRTextPoint if found, null otherwise
     */
    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints, String targetText) {
        if (textPoints == null) {
            return null;
        }
        for (OCRTextPoint textPoint : textPoints) {
            if (targetText.equals(textPoint.getText())) {
                return textPoint;
            }
        }
        return null;
    }

    /**
     * Prints all OCR text coordinates for debugging
     * @param textPoints List of OCR text points
     */
    private void printAllCoordinates(List<OCRTextPoint> textPoints) {
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("text =" + textPoint.getText() + " x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() + "\n\n\n\n");
        }
    }
}
