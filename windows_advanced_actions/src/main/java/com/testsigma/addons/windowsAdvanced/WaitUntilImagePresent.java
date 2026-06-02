package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.Constants;
import com.testsigma.addons.util.ResponseObjectForFindImage;
import com.testsigma.addons.util.ScreenshotUtils;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "Wait until image image-url is present on screen with timeout wait-time-in-seconds seconds with threshold threshold",
        description = "This action waits until the specified image appears on the screen within the given timeout. "
                + "It does not click the image; it only verifies that the image is present. "
                + "It takes an image URL (S3 URL or local file path), polls the screen every 1.5 seconds. "
                + "Threshold (0 to 1) controls match sensitivity. This works only for local executions.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Wait until image is present on screen",
        useCustomScreenshot = true)
public class WaitUntilImagePresent extends WindowsAdvancedAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData timeoutSeconds;

    @TestData(reference = "threshold")
    private com.testsigma.sdk.TestData threshold;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private final ObjectMapper mapper = new ObjectMapper();
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
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_image_failure_screenshot", logger);
                return Result.FAILED;
            }

            logger.info("Waiting for image from URL: " + imageUrlValue + " with timeout: "
                    + timeoutSeconds.getValue() + " seconds, threshold: " + thresholdStr);

            File searchImageFile = urlToFileConverter("target_image", imageUrlValue);
            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;

            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - checking for image on screen");

                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                File baseImageFile = saveScreenshotToFile(screenCapture, "wait_image_screenshot");

                int[] center = findImageCoordinates(baseImageFile, searchImageFile, thresholdStr);

                if (center != null) {
                    int centerX = center[0];
                    int centerY = center[1];
                    logger.info("Image found at center (" + centerX + ", " + centerY + "). Wait successful.");
                    setSuccessMessage("Image found on screen at coordinates (" + centerX + ", " + centerY + ").");
                    ScreenshotUtils.uploadScreenshotToS3(testStepResult, baseImageFile, logger);
                    return Result.SUCCESS;
                }

                if (baseImageFile.exists()) {
                    baseImageFile.delete();
                }

                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > POLLING_INTERVAL_MS) {
                    logger.info("Image not found yet. Waiting " + (POLLING_INTERVAL_MS / 1000)
                            + " second before next attempt. Remaining time: " + (remainingTime / 1000) + " seconds");
                    Thread.sleep(POLLING_INTERVAL_MS);
                } else {
                    break;
                }
            }

            logger.debug("Timeout reached. Image was not found on the screen within "
                    + timeoutSeconds.getValue() + " seconds.");
            setErrorMessage("Image was not found on the screen within " + timeoutSeconds.getValue() + " seconds.");
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_image_failure_screenshot", logger);
            return Result.FAILED;

        } catch (NumberFormatException e) {
            logger.debug("Invalid number format: " + e.getMessage());
            setErrorMessage("Invalid input. Timeout must be a number (seconds). Threshold must be a number between 0 and 1.");
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_image_failure_screenshot", logger);
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Exception during wait operation: " + e.getMessage());
            setErrorMessage("Error during wait operation: " + e.getMessage());
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "wait_image_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    /**
     * Finds the image on the screen and returns the center coordinates, or null if not found.
     * @param thresholdStr threshold for image match (0 to 1), from user input
     */
    private int[] findImageCoordinates(File baseImageFile, File searchImageFile, String thresholdStr) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("baseImageFile", baseImageFile.getName(),
                            RequestBody.create(baseImageFile, MediaType.parse("image/png")))
                    .addFormDataPart("searchImageFile", searchImageFile.getName(),
                            RequestBody.create(searchImageFile, MediaType.parse("image/png")))
                    .addFormDataPart("threshold", thresholdStr)
                    .addFormDataPart("scale", "40")
                    .addFormDataPart("occurance", "1")
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_FIND_IMAGE_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            String responseBody = response.body().string();
            ResponseObjectForFindImage responseObject = mapper.readValue(responseBody, ResponseObjectForFindImage.class);

            if (Boolean.TRUE.equals(responseObject.getIsFound())) {
                int x1 = responseObject.getX1();
                int y1 = responseObject.getY1();
                int x2 = responseObject.getX2();
                int y2 = responseObject.getY2();
                int centerX = (x1 + x2) / 2;
                int centerY = (y1 + y2) / 2;
                return new int[]{centerX, centerY};
            }
            return null;
        } catch (IOException e) {
            logger.debug("Exception while finding image: " + ExceptionUtils.getStackTrace(e));
            return null;
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private static File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        File tempFile = File.createTempFile(fileName, ".png");
        ImageIO.write(screenshot, "PNG", tempFile);
        return tempFile;
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
