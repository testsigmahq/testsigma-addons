package com.testsigma.addons.windowsLite;

import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import com.testsigma.sdk.ApplicationType;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "lite: Click on position position-type relative to the image image-url",
        description = "This action takes an image URL (S3 URL or local file path), finds that image on the current screen, " +
                "and clicks at a position relative to it. Position can be Left, Right, Top, Bottom, or Center of the image. " +
                "The action uses AI to locate the image within the screen and then performs the click at the specified relative position. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_UFT,
        displayName = "Click on position relative to image")
public class ClickOnPositionRelativeToImage extends WindowsAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestData(reference = "position-type", allowedValues = {"Left", "Right", "Top", "Bottom", "Center"})
    private com.testsigma.sdk.TestData position;

    @OCR
    private com.testsigma.sdk.OCR ocr;
    
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        logger.info("=== Click On Position Relative To Image: Starting Execution ===");

        try {
            String imageUrlValue = imageUrl.getValue().toString();
            String positionValue = position.getValue().toString();
            
            logger.info("Looking for image from URL: " + imageUrlValue + " to click at position: " + positionValue);

            // Convert URL to file
            File targetImageFile = urlToFileConverter("target_image", imageUrlValue);
            logger.info("Target image file prepared: " + targetImageFile.getAbsolutePath());

            // Capture the current screen
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());
            
            // Save the screenshot to a temporary file
            File baseImageFile = saveScreenshotToFile(screenCapture, "click_relative_image_screenshot");
            
            // Upload base image to S3 and use OCR to find target image
            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon s3 url in which we are storing base image: " + url);
            ocr.uploadFile(url, baseImageFile);
            logger.info("url: " + testStepResult.getScreenshotUrl());
            
            // Find the target image using OCR
            FindImageResponse responseObject = ocr.findImage(imageUrl.getValue().toString());
            ImageBoundingBox boundingBox = null;
            
            if (responseObject.getIsFound()) {
                // Extract coordinates from FindImageResponse
                int x1 = responseObject.getX1();
                int y1 = responseObject.getY1();
                int x2 = responseObject.getX2();
                int y2 = responseObject.getY2();
                
                // Create bounding box from OCR response
                boundingBox = new ImageBoundingBox(x1, y1, x2, y2, true);
                logger.info("Image found with bounding box: (" + boundingBox.getX1() + ", " + boundingBox.getY1() + 
                        ") to (" + boundingBox.getX2() + ", " + boundingBox.getY2() + ")");
                
                // Calculate click position based on bounding box and position parameter
                Point clickPoint = calculateClickPosition(boundingBox, positionValue);
                logger.info("Calculated click position: (" + clickPoint.x + ", " + clickPoint.y + ")");
                
                // Perform the click
                robot.mouseMove(clickPoint.x, clickPoint.y);
                Thread.sleep(100); // Small delay to ensure mouse is positioned
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(50);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                
                logger.info("Successfully clicked " + positionValue + " of image at coordinates (" + 
                        clickPoint.x + ", " + clickPoint.y + ")");
                setSuccessMessage("Successfully clicked " + positionValue + " of image at coordinates (" + 
                        clickPoint.x + ", " + clickPoint.y + ")");
                
                // Clean up temporary files
                cleanupFile(targetImageFile);
                cleanupFile(baseImageFile);
                
                return Result.SUCCESS;
            } else {
                logger.debug("Image not found on the screen");
                setErrorMessage("The specified image was not found on the screen. Unable to perform click.");
                
                // Clean up temporary files
                cleanupFile(targetImageFile);
                cleanupFile(baseImageFile);
                
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
            return Result.FAILED;
        }
    }

    /**
     * Calculates the click position based on image bounding box and position
     * @param boundingBox The image bounding box
     * @param position The relative position (Left, Right, Top, Bottom, Center)
     * @return Point with calculated click coordinates
     */
    private Point calculateClickPosition(ImageBoundingBox boundingBox, String position) {
        int centerX = (boundingBox.getX1() + boundingBox.getX2()) / 2;
        int centerY = (boundingBox.getY1() + boundingBox.getY2()) / 2;
        
        int clickX = centerX;
        int clickY = centerY;
        
        // Calculate position with a small offset from the edge (10 pixels)
        int edgeOffset = 10;
        
        switch (position.toUpperCase()) {
            case "LEFT":
                clickX = boundingBox.getX1() - edgeOffset;
                clickY = centerY;
                break;
            case "RIGHT":
                clickX = boundingBox.getX2() + edgeOffset;
                clickY = centerY;
                break;
            case "TOP":
                clickX = centerX;
                clickY = boundingBox.getY1() - edgeOffset;
                break;
            case "BOTTOM":
                clickX = centerX;
                clickY = boundingBox.getY2() + edgeOffset;
                break;
            case "CENTER":
                // For center, no offset needed
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
            javax.imageio.ImageIO.write(screenshot, "PNG", tempFile);
            return tempFile;
        } catch (Exception e) {
            logger.debug("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for AI processing.", e);
        }
    }

    /**
     * Converts URL to File - handles both S3 URLs and local file paths
     * @param fileName Base filename for temporary file
     * @param url The URL or file path
     * @return File object
     */
    public File urlToFileConverter(String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...File name:" + fileName);
                URL urlObject = new URL(url);
                String baseName = fileName;
                String extension = "";
                int lastDotIndex = fileName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    baseName = fileName.substring(0, lastDotIndex);
                    extension = fileName.substring(lastDotIndex);
                } else {
                    // Try to get extension from URL
                    String urlPath = urlObject.getPath();
                    int urlLastDotIndex = urlPath.lastIndexOf('.');
                    if (urlLastDotIndex > 0) {
                        extension = urlPath.substring(urlLastDotIndex);
                    } else {
                        extension = ".png"; // Default to PNG for images
                    }
                }
                
                File tempFile = File.createTempFile(baseName, extension);
                
                // Download file from URL
                try (InputStream in = urlObject.openStream()) {
                    Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                
                logger.info("Temp file created with name for s3 file " + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given file, please check the given inputs.");
        }
    }

    /**
     * Cleans up temporary file
     * @param file File to delete
     */
    private void cleanupFile(File file) {
        try {
            if (file != null && file.exists() && file.isFile()) {
                if (file.delete()) {
                    logger.debug("Cleaned up temporary file: " + file.getAbsolutePath());
                } else {
                    logger.debug("Failed to delete temporary file: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.debug("Error cleaning up file: " + e.getMessage());
        }
    }

    /**
     * Inner class to hold image bounding box information
     */
    private static class ImageBoundingBox {
        private final int x1;
        private final int y1;
        private final int x2;
        private final int y2;

        public ImageBoundingBox(int x1, int y1, int x2, int y2, boolean found) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public int getX1() { return x1; }
        public int getY1() { return y1; }
        public int getX2() { return x2; }
        public int getY2() { return y2; }
    }
}
