package com.testsigma.addons.windowsLite;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "wait until image image-url is present on screen with timeout wait-time-in-seconds seconds " +
        "with threshold threshold-value (Ex: 0.9 , means 90% match)",
        description = "This action waits until the specified image appears on the screen within the given timeout. "
                + "It does not click the image; it only verifies that the image is present. "
                + "It takes an image URL (S3 URL or local file path), polls the screen every 1.5 seconds. "
                + "Threshold (0 to 1) controls match sensitivity",
        applicationType = ApplicationType.WINDOWS)
public class WaitUntilImagePresent extends WindowsAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData timeoutSeconds;

    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData threshold;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int POLLING_INTERVAL_MS = 1500;

    @Override
    protected Result execute() {
        logger.info("=== Wait Until Image Present: Starting Execution ===");

        try {
            Result result = Result.SUCCESS;
            String imageUrlValue = imageUrl.getValue().toString();
            int timeoutMs = Integer.parseInt(timeoutSeconds.getValue().toString()) * 1000;
            String thresholdStr = threshold.getValue().toString().trim();

            logger.info("Waiting for image from URL: " + imageUrlValue + " with timeout: "
                    + timeoutSeconds.getValue() + " seconds, threshold: " + thresholdStr);

            File searchImageFile = urlToFileConverter("target_image", imageUrlValue);
            Robot robot = new Robot();
            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;

            while (System.currentTimeMillis() < endTime) {
                try {
                    // Fetch the Details of the Screen Size
                    Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

                    // Take the Snapshot of the Screen
                    BufferedImage tmp = robot.createScreenCapture(screenSize);

                    // Provide the destination details to copy the screenshot
                    String tempDir = System.getProperty("java.io.tmpdir");
                    String filename = "screenshot"+System.currentTimeMillis()+".jpg";
                    String path = tempDir + filename;

                    // To copy source image in to destination path
                    ImageIO.write(tmp, "jpg",new File(path));
                    int width = tmp.getWidth();
                    int height = tmp.getHeight();
                    logger.info("Width of image: " + width);
                    logger.info("Height of image: " + height);

                    File baseImageFile = new File(path);
                    String url = testStepResult.getScreenshotUrl();
                    ocr.uploadFile(url, baseImageFile);
                    logger.info("url: "+ testStepResult.getScreenshotUrl());
                    FindImageResponse responseObject = ocr.findImage(imageUrl.getValue().toString(),
                            Float.valueOf(threshold.getValue().toString()));
                    if (responseObject.getIsFound()){
                        boolean isFound = responseObject.getIsFound();
                        int x1 = responseObject.getX1();
                        int y1 = responseObject.getY1();
                        int x2 = responseObject.getX2();
                        int y2 = responseObject.getY2();

                        int clickLocationX = (x1 + x2) / 2;
                        int clickLocationY = (y1 + y2) / 2;

                        logger.info("Click Location X: " + clickLocationX);
                        logger.info("Click Location Y: " + clickLocationY);


                        setSuccessMessage("Image Found :" + isFound +
                                "    Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
                        Thread.sleep(1000);
                        return Result.SUCCESS;
                    } else {
                        setErrorMessage("Unable to fetch the coordinates");
                        result = Result.FAILED;
                        return result;
                    }
                }
                catch (Exception e){
                    logger.info("Exception: "+ ExceptionUtils.getStackTrace(e));
                    setErrorMessage("Exception occurred while performing click action");
                    result = Result.FAILED;
                    return result;
                }
            }

            logger.debug("Timeout reached. Image was not found on the screen within "
                    + timeoutSeconds.getValue() + " seconds.");
            setErrorMessage("Image was not found on the screen within " + timeoutSeconds.getValue() + " seconds.");
            return Result.FAILED;
        } catch (NumberFormatException e) {
            logger.debug("Invalid number format: " + e.getMessage());
            setErrorMessage("Invalid input. Timeout must be a number (seconds). Threshold must be a number between 0 and 1.");
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during wait operation: " + e.getMessage());
            setErrorMessage("Error during wait operation: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private File urlToFileConverter(String fileName, String url) {
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
                    String urlPath = urlObject.getPath();
                    int urlLastDotIndex = urlPath.lastIndexOf('.');
                    if (urlLastDotIndex > 0) {
                        extension = urlPath.substring(urlLastDotIndex);
                    } else {
                        extension = ".png";
                    }
                }
                File tempFile = File.createTempFile(baseName, extension);
                try (InputStream in = urlObject.openStream()) {
                    Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                logger.info("Temp file created: " + tempFile.getName() + " at " + tempFile.getAbsolutePath());
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
}

/*
package com.testsigma.addons.windowsLite;

import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "Wait until image image-url is present on screen with timeout wait-time-in-seconds seconds with threshold threshold-value",
        description = "This action waits until the specified image appears on the screen within the given timeout. "
                + "It does not click the image; it only verifies that the image is present. "
                + "It takes an image URL (S3 URL or local file path), polls the screen every 1.5 seconds. "
                + "Threshold (0 to 1) controls match sensitivity",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class WaitUntilImagePresent extends WindowsAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData timeoutSeconds;

    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData threshold;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int POLLING_INTERVAL_MS = 1500;

    @Override
    protected Result execute() {
        logger.info("=== Wait Until Image Present: Starting Execution ===");

        try {
            String imageUrlValue = imageUrl.getValue().toString();
            int timeoutMs = Integer.parseInt(timeoutSeconds.getValue().toString()) * 1000;
            String thresholdStr = threshold.getValue().toString().trim();
            double thresholdValue = Double.parseDouble(thresholdStr);
            if (thresholdValue < 0 || thresholdValue > 1) {
                setErrorMessage("Threshold must be between 0 and 1. Got: " + thresholdStr);
                return Result.FAILED;
            }

            logger.info("Waiting for image from URL: " + imageUrlValue + " with timeout: "
                    + timeoutSeconds.getValue() + " seconds, threshold: " + thresholdStr);

            File searchImageFile = urlToFileConverter("target_image", imageUrlValue);
            Robot robot = new Robot();
            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;

            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - capturing fresh screenshot and checking for image on screen");

                Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenSize);
                File screenshotFile = new File(System.getProperty("java.io.tmpdir"),
                        "screenshot" + System.currentTimeMillis() + ".png");
                String screenshotPath = screenshotFile.getAbsolutePath();
                ImageIO.write(screenCapture, "png", screenshotFile);
                logger.info("Screenshot saved to: " + screenshotPath);

                ocr.uploadFile(testStepResult.getScreenshotUrl(), screenshotFile);
                logger.info("Screenshot uploaded to: " + testStepResult.getScreenshotUrl());

                float thresholdPercentage = (float) thresholdValue;
                logger.info("Threshold percentage: " + thresholdPercentage);
                FindImageResponse findImageResponse = ocr.findImage(searchImageFile.getAbsolutePath(), thresholdPercentage);

                if (findImageResponse != null && findImageResponse.getIsFound()) {
                    int centerX = findImageResponse.getX1() + (findImageResponse.getX2() - findImageResponse.getX1()) / 2;
                    int centerY = findImageResponse.getY1() + (findImageResponse.getY2() - findImageResponse.getY1()) / 2;
                    logger.info("Image found at center (" + centerX + ", " + centerY + "). Wait successful.");
                    setSuccessMessage("Image found on screen at coordinates (" + centerX + ", " + centerY + ").");
                    return Result.SUCCESS;
                }

                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > 0) {
                    long sleepTime = Math.min(POLLING_INTERVAL_MS, remainingTime);
                    logger.info("Image not found yet. Waiting " + sleepTime + "ms before next attempt. Remaining time: " + remainingTime + "ms");
                    Thread.sleep(sleepTime);
                }
            }

            logger.debug("Timeout reached. Image was not found on the screen within "
                    + timeoutSeconds.getValue() + " seconds.");
            setErrorMessage("Image was not found on the screen within " + timeoutSeconds.getValue() + " seconds.");
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid number format: " + e.getMessage());
            setErrorMessage("Invalid input. Timeout must be a number (seconds). Threshold must be a number between 0 and 1.");
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during wait operation: " + e.getMessage());
            setErrorMessage("Error during wait operation: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private File urlToFileConverter(String fileName, String url) {
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
                    String urlPath = urlObject.getPath();
                    int urlLastDotIndex = urlPath.lastIndexOf('.');
                    if (urlLastDotIndex > 0) {
                        extension = urlPath.substring(urlLastDotIndex);
                    } else {
                        extension = ".png";
                    }
                }
                File tempFile = File.createTempFile(baseName, extension);
                try (InputStream in = urlObject.openStream()) {
                    Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                logger.info("Temp file created: " + tempFile.getName() + " at " + tempFile.getAbsolutePath());
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
}
*/

