package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import com.testsigma.sdk.TestStepResult;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility class for screenshot operations and S3 upload
 */
public class ScreenshotUtils {
    
    private static final RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(30000)
            .setConnectionRequestTimeout(30000)
            .setSocketTimeout(30000)
            .build();

    /**
     * Captures and uploads screenshot to S3
     * @param testStepResult The test step result containing S3 URL
     * @param screenshotName The name for the screenshot
     * @param logger The logger instance
     * @return true if upload was successful, false otherwise
     */
    public static boolean captureAndUploadScreenshot(TestStepResult testStepResult, String screenshotName, Logger logger) {
        try {
            // Wait for 1 second before capturing screenshot to ensure UI is stable
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                logger.info("ignored the interrupted exception during screenshot capture wait: "
                        + ExceptionUtils.getStackTrace(interruptedException));
            }
            
            // Capture the current screen
            Robot robot = new Robot();
            
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            
            // Save screenshot to temporary file
            File screenshotFile = saveScreenshotToFile(screenCapture, screenshotName);
            
            // Upload to S3
            boolean uploadResult = uploadScreenshotToS3(testStepResult, screenshotFile, logger);
            
            // Clean up temporary file
            if (screenshotFile.exists()) {
                screenshotFile.delete();
            }
            
            return uploadResult;
            
        } catch (Exception e) {
            logger.info("Exception during screenshot capture and upload: " + ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     * Uploads an existing screenshot file to S3
     * @param testStepResult The test step result containing S3 URL
     * @param screenshotFile The screenshot file to upload
     * @param logger The logger instance
     * @return true if upload was successful, false otherwise
     */
    public static boolean uploadScreenshotToS3(TestStepResult testStepResult, File screenshotFile, Logger logger) {
        try {
            String s3Url = testStepResult.getScreenshotUrl();
            
            if (s3Url != null && !s3Url.isEmpty() && screenshotFile.exists()) {
                boolean uploadResult = uploadFile(s3Url, screenshotFile.getAbsolutePath(), logger);
                if (uploadResult) {
                    logger.info("Successfully uploaded screenshot to S3: " + s3Url);
                    return true;
                } else {
                    logger.info("Error occurred while uploading screenshot to S3");
                    return false;
                }
            } else {
                logger.info("S3 URL is null or empty, or screenshot file doesn't exist, skipping screenshot upload");
                return false;
            }
        } catch (Exception e) {
            logger.info("Exception during screenshot upload: " + ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     * Saves the screenshot to a temporary file
     * @param screenshot The captured screenshot
     * @param fileName The base filename
     * @return The temporary file
     * @throws Exception if file creation fails
     */
    public static File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException("Unable to save screenshot for processing.", e);
        }
    }

    /**
     * Uploads a file to S3 using the provided URL
     * 
     * @param s3SignedURL The S3 signed URL to upload to
     * @param localPath The absolute path of the file to upload
     * @param logger The logger instance
     * @return true if upload was successful, false otherwise
     */
    public static boolean uploadFile(String s3SignedURL, String localPath, Logger logger) {
        logger.info("s3SignedURL - " + s3SignedURL);
        logger.info("localPath - " + localPath);
        boolean localUrlExists = new File(localPath).exists();
        if (localUrlExists) {
            logger.info(String.format("Uploading test asset to storage, presigned-URL:%s, localFilePath:%s", 
                    s3SignedURL, localPath));
            try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
                HttpPut httpPut = new HttpPut(s3SignedURL);

                File file = new File(localPath);
                HttpEntity entity = new FileEntity(file);
                httpPut.setEntity(entity);
                HttpResponse response = httpclient.execute(httpPut);
                if(response.getStatusLine().getStatusCode() == 200) {
                    logger.info("Upload completed");
                    return true;
                } else {
                    logger.info("Upload failed with status code: " + response.getStatusLine().getStatusCode());
                    return false;
                }
            } catch (Exception e) {
                logger.info("Exception while uploading custom screenshot to s3: " 
                        + ExceptionUtils.getStackTrace(e));
                return false;
            }
        } else {
            logger.info("Local path does not exist");
            return false;
        }
    }
}
