package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.Constants;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "Wait until image image-url is present on screen with timeout wait-time-in-seconds seconds with threshold threshold-value",
        description = "This action waits until the specified image appears on the screen within the given timeout. "
                + "It does not click the image; it only verifies that the image is present. "
                + "It takes an image URL (S3 URL or local file path), polls the screen every 1.5 seconds. "
                + "Threshold (0 to 1) controls match sensitivity. Uses visual testing API for image detection.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Wait until image is present (Find Image API)",
        useCustomScreenshot = true)
public class WaitUntilImagePresentWithFindImage extends WindowsAdvancedAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestData(reference = "wait-time-in-seconds")
    private com.testsigma.sdk.TestData timeoutSeconds;

    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData thresholdValue;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int POLLING_INTERVAL_MS = 1500;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected Result execute() {
        logger.info("=== Wait Until Image Present (Find Image API): Starting Execution ===");

        try {
            String imageUrlValue = imageUrl.getValue().toString();
            int timeoutMs = Integer.parseInt(timeoutSeconds.getValue().toString()) * 1000;
            String thresholdStr = thresholdValue.getValue().toString().trim();
            double threshold = Double.parseDouble(thresholdStr);
            if (threshold < 0 || threshold > 1) {
                setErrorMessage("Threshold must be between 0 and 1. Got: " + thresholdStr);
                return Result.FAILED;
            }

            logger.info("Waiting for image: " + imageUrlValue + " | timeout: "
                    + timeoutSeconds.getValue() + "s | threshold: " + thresholdStr);

            File searchImageFile = urlToFileConverter("target_image", imageUrlValue);
            Robot robot = new Robot();
            long endTime = System.currentTimeMillis() + timeoutMs;

            while (System.currentTimeMillis() < endTime) {
                logger.info("Polling attempt - capturing fresh screenshot");

                Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage screenCapture = robot.createScreenCapture(screenSize);
                File screenshotFile = new File(System.getProperty("java.io.tmpdir"),
                        "screenshot" + System.currentTimeMillis() + ".png");
                ImageIO.write(screenCapture, "png", screenshotFile);
                logger.info("Screenshot saved to: " + screenshotFile.getAbsolutePath());

                boolean isFound = callFindImageApi(screenshotFile, searchImageFile, thresholdStr);

                if (isFound) {
                    logger.info("Image found on screen. Wait successful.");
                    return Result.SUCCESS;
                }

                long remainingTime = endTime - System.currentTimeMillis();
                if (remainingTime > 0) {
                    long sleepTime = Math.min(POLLING_INTERVAL_MS, remainingTime);
                    logger.info("Image not found yet. Waiting " + sleepTime + "ms. Remaining: " + remainingTime + "ms");
                    Thread.sleep(sleepTime);
                }
            }

            logger.debug("Timeout reached. Image was not found within " + timeoutSeconds.getValue() + " seconds.");
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

    /**
     * Calls the visual testing API to check whether the search image is present
     * in the base screenshot. Returns true if found, false otherwise.
     * Sets success/error message accordingly.
     */
    private boolean callFindImageApi(File baseImageFile, File searchImageFile, String threshold) {
        try {
            logger.info("Calling visual testing API | base: " + baseImageFile + " | search: " + searchImageFile);
            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("baseImageFile", baseImageFile.getName(),
                            RequestBody.create(baseImageFile, MediaType.parse("image/png")))
                    .addFormDataPart("searchImageFile", searchImageFile.getName(),
                            RequestBody.create(searchImageFile, MediaType.parse("image/png")))
                    .addFormDataPart("threshold", threshold)
                    .addFormDataPart("scale", "40")
                    .addFormDataPart("occurance", "1")
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_FIND_IMAGE_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                logger.info("API response: " + responseBody);
                JsonNode jsonNode = mapper.readTree(responseBody);

                boolean isFound = jsonNode.path("isFound").asBoolean();
                int x1 = jsonNode.path("x1").asInt();
                int y1 = jsonNode.path("y1").asInt();
                int x2 = jsonNode.path("x2").asInt();
                int y2 = jsonNode.path("y2").asInt();

                if (isFound) {
                    int centerX = x1 + (x2 - x1) / 2;
                    int centerY = y1 + (y2 - y1) / 2;
                    logger.info("Image found at center (" + centerX + ", " + centerY + ")");
                    setSuccessMessage(String.format(
                            "Image found on screen. Coordinates: x1-%s, x2-%s, y1-%s, y2-%s",
                            x1, x2, y1, y2));
                } else {
                    logger.info("Image not found in this poll attempt");
                }
                return isFound;

            } else {
                logger.info("API call failed or returned empty body. Code: "
                        + (response.body() != null ? response.code() : "no body"));
                return false;
            }

        } catch (Exception e) {
            logger.info("Exception during API call: " + ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private File urlToFileConverter(String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Downloading image from URL: " + url);
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
                logger.info("Using local file path: " + url);
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given file, please check the given inputs.");
        }
    }
}
