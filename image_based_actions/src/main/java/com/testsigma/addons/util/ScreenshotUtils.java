package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import com.testsigma.sdk.TestStepResult;
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

public class ScreenshotUtils {

    private static final RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(30000)
            .setConnectionRequestTimeout(30000)
            .setSocketTimeout(30000)
            .build();

    /**
     * Draws a red bounding box and green crosshair at the matched location on the image.
     */
    public static BufferedImage highlightClickLocation(BufferedImage image,
                                                       int x1, int y1, int x2, int y2) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(image, 0, 0, null);

        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(x1, y1, x2 - x1, y2 - y1);

        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(centerX-1, centerY-1, 1, 1); // small square at center
//        int crossSize = 15;
//        g2d.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY);
//        g2d.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize);
//        g2d.drawOval(centerX - crossSize, centerY - crossSize, crossSize * 2, crossSize * 2);

        g2d.dispose();
        return copy;
    }

    public static File saveScreenshotToFile(BufferedImage image, String fileName) throws Exception {
        File tempFile = File.createTempFile(fileName, ".png");
        ImageIO.write(image, "PNG", tempFile);
        return tempFile;
    }

    public static boolean uploadScreenshotToS3(TestStepResult testStepResult, File screenshotFile, Logger logger) {
        try {
            String s3Url = testStepResult.getScreenshotUrl();
            if (s3Url != null && !s3Url.isEmpty() && screenshotFile.exists()) {
                boolean result = uploadFile(s3Url, screenshotFile.getAbsolutePath(), logger);
                if (result) {
                    logger.info("Successfully uploaded screenshot to S3");
                } else {
                    logger.info("Failed to upload screenshot to S3");
                }
                return result;
            } else {
                logger.info("S3 URL is null/empty or screenshot file doesn't exist, skipping upload");
                return false;
            }
        } catch (Exception e) {
            logger.info("Exception during screenshot upload: " + e.getMessage());
            return false;
        }
    }

    /**
     * Highlights the matched region, saves to a temp file, and uploads to S3.
     * Returns the temp file (caller should clean up).
     */
    public static File highlightAndUpload(BufferedImage baseImage, int x1, int y1, int x2, int y2,
                                          String filePrefix, TestStepResult testStepResult, Logger logger) {
        File highlightedFile = null;
        try {
            BufferedImage highlighted = highlightClickLocation(baseImage, x1, y1, x2, y2);
            highlightedFile = saveScreenshotToFile(highlighted, filePrefix);
            uploadScreenshotToS3(testStepResult, highlightedFile, logger);
        } catch (Exception e) {
            logger.info("Error during highlight and upload: " + e.getMessage());
        }
        return highlightedFile;
    }

    /**
     * Uploads a plain (non-highlighted) screenshot — used on failure paths.
     */
    public static void uploadPlainScreenshot(File screenshotFile, TestStepResult testStepResult, Logger logger) {
        try {
            uploadScreenshotToS3(testStepResult, screenshotFile, logger);
        } catch (Exception e) {
            logger.info("Error uploading plain screenshot: " + e.getMessage());
        }
    }

    private static boolean uploadFile(String s3SignedURL, String localPath, Logger logger) {
        logger.info("Uploading to S3, presigned-URL: " + s3SignedURL);
        File file = new File(localPath);
        if (!file.exists()) {
            logger.info("Local file does not exist: " + localPath);
            return false;
        }
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpPut httpPut = new HttpPut(s3SignedURL);
            httpPut.setEntity(new FileEntity(file));
            HttpResponse response = httpclient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                logger.info("Upload completed successfully");
                return true;
            } else {
                logger.info("Upload failed with status code: " + statusCode);
                return false;
            }
        } catch (Exception e) {
            logger.info("Exception while uploading screenshot to S3: " + e.getMessage());
            return false;
        }
    }
}
