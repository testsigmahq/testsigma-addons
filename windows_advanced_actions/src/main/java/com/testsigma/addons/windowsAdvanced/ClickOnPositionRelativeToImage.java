package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.apache.http.client.config.RequestConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Action(actionText = "Click on position position-type relative to the image image-url with pixel offset pixel-offset",
        description = "This action takes an image URL (S3 URL or local file path), finds that image on the current screen, " +
                "and clicks at a position relative to it with a pixel offset. Position can be Left, Right, Top, Bottom, or Center of the image. " +
                "The pixel offset determines how far from the image edge to click (positive values move away from image, negative values move towards image). " +
                "For Center position, offset is ignored. " +
                "The action uses AI to locate the image within the screen and then performs the click at the specified relative position. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Click on position relative to image",
        useCustomScreenshot = true)
public class ClickOnPositionRelativeToImage extends WindowsAdvancedAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestData(reference = "position-type", allowedValues = {"Left", "Right", "Top", "Bottom", "Center"})
    private com.testsigma.sdk.TestData position;

    @TestData(reference = "pixel-offset")
    private com.testsigma.sdk.TestData pixelOffset;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    RequestConfig config = RequestConfig.custom().setSocketTimeout(10 * 60 * 1000)
            .setConnectionRequestTimeout(60 * 1000).setConnectTimeout(60 * 1000).build();
    ObjectMapper mapper = new ObjectMapper();
    ResponseObjectForFindImage responseObjectForFindImage = new ResponseObjectForFindImage();

    @Override
    protected Result execute() {
        logger.info("=== Click On Position Relative To Image: Starting Execution ===");

        try {
            String imageUrlValue = imageUrl.getValue().toString();
            String positionValue = position.getValue().toString();
            int offset = Integer.parseInt(pixelOffset.getValue().toString());

            logger.info("Looking for image from URL: " + imageUrlValue + " to click at position: " + positionValue +
                    " with offset: " + offset + " pixels");

            // Convert URL to file
            File searchImageFile = urlToFileConverter("target_image", imageUrlValue);
            logger.info("Target image file prepared: " + searchImageFile.getAbsolutePath());

            // Capture the current screen
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            // Save the screenshot to a temporary file
            File baseImageFile = saveScreenshotToFile(screenCapture, "click_relative_image_screenshot");

            // Call visual testing API to find the image
            performApiCall(baseImageFile, searchImageFile, positionValue, offset);
            logger.info("Visual testing API call completed");

            // Capture and upload screenshot on success
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_relative_image_success_screenshot", logger);

            // Clean up temporary files
            cleanupFile(searchImageFile);
            cleanupFile(baseImageFile);

            return Result.SUCCESS;

        } catch (Exception e) {
            logger.debug("Exception during click operation: " + e.getMessage());
            setErrorMessage("Error during click operation: " + e.getMessage());

            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "click_relative_image_failure_screenshot", logger);

            return Result.FAILED;
        }
    }

    /**
     * Performs click using Robot with appropriate delays
     *
     * @param x X coordinate for click
     * @param y Y coordinate for click
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
     * Calculates the click position based on image coordinates, position, and offset
     * @param x1 Left coordinate of image
     * @param y1 Top coordinate of image
     * @param x2 Right coordinate of image
     * @param y2 Bottom coordinate of image
     * @param position The relative position (Left, Right, Top, Bottom, Center)
     * @param offset The pixel offset from the image
     * @return Point with calculated click coordinates
     */
    private Point calculateClickPosition(int x1, int y1, int x2, int y2, String position, int offset) {
        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;

        int clickX = centerX;
        int clickY = centerY;

        switch (position.toUpperCase()) {
            case "LEFT":
                clickX = x1 - offset;
                clickY = centerY;
                break;
            case "RIGHT":
                clickX = x2 + offset;
                clickY = centerY;
                break;
            case "TOP":
                clickX = centerX;
                clickY = y1 - offset;
                break;
            case "BOTTOM":
                clickX = centerX;
                clickY = y2 + offset;
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

    public void performApiCall(File baseImageFile, File searchImageFile, String position, int offset) {
        try {
            logger.info("Performing visual testing with files: " + baseImageFile + " and " + searchImageFile);
            OkHttpClient client = new OkHttpClient();
            logger.info("Initiating http client");

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("baseImageFile", baseImageFile.getName(),
                            RequestBody.create(baseImageFile, MediaType.parse("image/png")))
                    .addFormDataPart("searchImageFile", searchImageFile.getName(),
                            RequestBody.create(searchImageFile, MediaType.parse("image/png")))
                    .addFormDataPart("threshold", "0.7")
                    .addFormDataPart("scale", "40")
                    .addFormDataPart("occurance", "1")
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_FIND_IMAGE_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();

            logger.info("Making api call to visual server");
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                logger.info("Response is successful");
                if (response.body() != null) {
                    logger.info("Response body received");
                    String responseBody = response.body().string();
                    logger.info("Response body for testing: " + responseBody);
                    responseObjectForFindImage = mapper.readValue(responseBody, ResponseObjectForFindImage.class);
                    logger.info("Deserialized the response body");
                    JsonNode jsonNode = mapper.readTree(responseBody);

                    boolean isFound = jsonNode.path("isFound").asBoolean();
                    int x1 = jsonNode.path("x1").asInt();
                    int y1 = jsonNode.path("y1").asInt();
                    int x2 = jsonNode.path("x2").asInt();
                    int y2 = jsonNode.path("y2").asInt();

                    if (isFound) {
                        // Calculate click position based on position parameter and offset
                        Point clickPoint = calculateClickPosition(x1, y1, x2, y2, position, offset);

                        logger.info("Image found with coordinates: x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2);
                        logger.info("Calculated click position: (" + clickPoint.x + ", " + clickPoint.y + ") with offset: " + offset + " pixels");

                        performClickWithRobot(clickPoint.x, clickPoint.y);
                        setSuccessMessage(String.format(
                                "Successfully clicked %s of image with offset %d pixels at coordinates: x-<b>%d</b>, y-<b>%d</b>. Image coordinates: x1-<b>%d</b>, x2-<b>%d</b>, y1-<b>%d</b>, y2-<b>%d</b>",
                                position, offset, clickPoint.x, clickPoint.y, x1, x2, y1, y2
                        ));
                    } else {
                        setErrorMessage("Image NOT Found");
                        throw new RuntimeException("Visual testing failed as image not found on the screen");
                    }
                } else {
                    logger.info("Response body is null");
                    setErrorMessage("Visual testing failed. no response body present in the visual test response");
                    throw new RuntimeException("Visual testing failed with no response body");
                }
            } else {
                setErrorMessage("Visual testing failed error occurred internally");
                throw new RuntimeException("Visual testing failed with internal server error");
            }
        } catch (IOException e) {
            logger.info("Exception occurred while performing visual test: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform visual testing");
            throw new RuntimeException("Error occurred while performing visual test");
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

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
