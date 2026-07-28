package com.testsigma.addons.windowsLite;

import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.ApplicationType;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "lite: Click on image image-url",
        description = "This action takes an image URL (S3 URL or local file path), finds that image on the current screen, " +
                "and clicks on it. The action uses AI to locate the image within the screen. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_UFT,
        displayName = "Click on image")
public class ClickOnImage extends WindowsAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        logger.info("=== Click On Image: Starting Execution ===");

        try {
            String imageUrlValue = imageUrl.getValue().toString();
            logger.info("Looking for image from URL: " + imageUrlValue);

            // Convert URL to file
            File targetImageFile = urlToFileConverter("target_image", imageUrlValue);
            logger.info("Target image file prepared: " + targetImageFile.getAbsolutePath());

            // Capture the current screen
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            // Save the screenshot to a temporary file
            File baseImageFile = saveScreenshotToFile(screenCapture, "click_image_screenshot");

            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon s3 url in which we are storing base image" + url);
            ocr.uploadFile(url, baseImageFile);
            logger.info("url: " + testStepResult.getScreenshotUrl());
            FindImageResponse responseObject = ocr.findImage(imageUrl.getValue().toString());
            if (responseObject.getIsFound()) {
                boolean isFound = responseObject.getIsFound();
                int x1 = responseObject.getX1();
                int y1 = responseObject.getY1();
                int x2 = responseObject.getX2();
                int y2 = responseObject.getY2();

                int clickLocationX = (x1 + x2) / 2;
                int clickLocationY = (y1 + y2) / 2;

                logger.info("Click Location X: " + clickLocationX);
                logger.info("Click Location Y: " + clickLocationY);
                // Perform the click
                robot.mouseMove(clickLocationX, clickLocationY);
                Thread.sleep(100); // Small delay to ensure mouse is positioned
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(50);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                logger.info("Successfully clicked on image at coordinates (" +
                        clickLocationX + ", " + clickLocationY + ")");
                setSuccessMessage("Successfully clicked on image at coordinates (" +
                        clickLocationX + ", " + clickLocationY + ")");
                setSuccessMessage("Image Found :" + isFound +
                        "    Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
                Thread.sleep(2000);
            } else {
                setErrorMessage("Unable to fetch the coordinates");
                return Result.FAILED;
            }
            // Clean up temporary files
            cleanupFile(targetImageFile);
            cleanupFile(baseImageFile);

            return Result.SUCCESS;

        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());
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
            javax.imageio.ImageIO.write(screenshot, "PNG", tempFile);
            return tempFile;
        } catch (Exception e) {
            logger.debug("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for AI processing.", e);
        }
    }

    /**
     * Converts URL to File - handles both S3 URLs and local file paths
     *
     * @param fileName Base filename for temporary file
     * @param url      The URL or file path
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
     *
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
}
