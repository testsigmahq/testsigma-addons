package com.testsigma.addons.android;

import com.testsigma.addons.util.ImageComparisonUtils;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.*;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.NoSuchElementException;

@Data
@Action(actionText = "Download image from the <img> tag using element element-locator and store the file path of " +
        "saved image in runtime variable variable_name",
        description = "Download image from the <img> tag and store the file path of the saved image" +
                " in a runtime variable.",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = true
)
public class DownloadImageFromImgTag extends AndroidAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element elementLocator;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            logger.info("initiating execution of DownloadImageFromImgTag action");

            String basePdfDirectoryPath = String.valueOf(Files.createTempDirectory("basePdfDirectory"));
            logger.info("Base PDF Directory Path: " + basePdfDirectoryPath);
            String timeNow = String.valueOf(System.currentTimeMillis());
            File file1 = new File(basePdfDirectoryPath + File.separator
                    + "element_image_" + timeNow + ".png");

            WebElement webElement = elementLocator.getElement();
            AndroidDriver androidDriver = (AndroidDriver) driver;
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            String tagName = webElement.getTagName().toLowerCase();
            byte[] imageBytes;
            Dimension elementSize = webElement.getSize();
            if ("img".equals(tagName)) {
                // First try to get src attribute normally
                String imageSrc = webElement.getAttribute("src");

                // If no src found or element might be in shadow root, try JavaScript approach
                if (imageSrc == null || imageSrc.isEmpty()) {
                    logger.info("No src attribute found, attempting to access shadow root");
                    imageSrc = getSrcFromShadowRoot(jsExecutor, webElement);
                }

                if (imageSrc != null && !imageSrc.isEmpty()) {
                    logger.info("Found img element with src: " + imageSrc);
                    imageBytes = downloadImageFromUrl(androidDriver, imageSrc);
                } else {
                    logger.info("Could not find image src, taking element screenshot");
                    imageBytes = webElement.getScreenshotAs(OutputType.BYTES);
                }
            } else {
                // Check if element contains shadow root with images
                logger.info("Element is not an img tag, checking for shadow root images");
                String shadowImageSrc = findImageInShadowRoot(jsExecutor, webElement);

                if (shadowImageSrc != null && !shadowImageSrc.isEmpty()) {
                    logger.info("Found image in shadow root with src: " + shadowImageSrc);
                    imageBytes = downloadImageFromUrl(androidDriver, shadowImageSrc);
                } else {
                    logger.info("No shadow root images found, taking element screenshot");
                    imageBytes = webElement.getScreenshotAs(OutputType.BYTES);
                }
            }

            logger.info("Image captured successfully");
            saveBytesArrayToFile(file1.getAbsolutePath(), imageBytes);
            logger.info("Image saved at: " + file1.getAbsolutePath());

            String s3Url = testStepResult.getScreenshotUrl();
            ImageComparisonUtils imageComparisonUtils = new ImageComparisonUtils(androidDriver, logger);
            logger.info("images are identical hence uploading the actual image to S3");
            boolean uploadS3Result = imageComparisonUtils.uploadFile(s3Url, file1.getAbsolutePath());
            if (!uploadS3Result) {
                logger.info("Error occurred while uploading combined image to s3," +
                        " screenshot might not be displayed");
            }
            logger.info("Upload complete.");
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(file1.getAbsolutePath());
            setSuccessMessage("Successfully downloaded the image with dimensions " + elementSize.getWidth() + "x"
                    + elementSize.getHeight() + " and stored the file path in runtime variable: " +
                    variableName.getValue().toString());
        } catch (NoSuchElementException ne) {
            logger.info("Element not found: " + ExceptionUtils.getStackTrace(ne));
            setErrorMessage("Element not found: " + ne.getMessage());
            result = com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Failed to download image: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to download image: " + e.getMessage());
        }
        return result;
    }


    public void saveBytesArrayToFile(String filePath, byte[] imageBytes) {
        try {
            File file = new File(filePath);
            boolean isCreated = file.createNewFile();
            logger.debug("is file created - " + isCreated);
            FileUtils.writeByteArrayToFile(file, imageBytes);
        } catch (IOException e) {
            logger.info("Failed to save byte[] image to path " + filePath);
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }

    // Helper method to get src from shadow root
    private String getSrcFromShadowRoot(JavascriptExecutor jsExecutor, WebElement element) {
        try {
            String script = """
                        var element = arguments[0];
                        if (element.shadowRoot) {
                            var imgElement = element.shadowRoot.querySelector('img');
                            if (imgElement) {
                                return imgElement.src || imgElement.getAttribute('src');
                            }
                        }
                        return null;
                    """;

            Object result = jsExecutor.executeScript(script, element);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            logger.info("Error accessing shadow root: " + e.getMessage());
            return null;
        }
    }

    // Helper method to find images in shadow root - Enhanced for chart components
    private static String findImageInShadowRoot(JavascriptExecutor jsExecutor, WebElement element) {
        try {
            String script = """
                        var element = arguments[0];
                        
                        // Function to recursively search for images in shadow roots
                        function findImageInShadow(el) {
                            if (el.shadowRoot) {
                                // Look for img elements in shadow root
                                var imgElements = el.shadowRoot.querySelectorAll('img');
                                if (imgElements.length > 0) {
                                    return imgElements[0].src || imgElements[0].getAttribute('src');
                                }
                                
                                // Look for canvas elements (charts often use canvas)
                                var canvasElements = el.shadowRoot.querySelectorAll('canvas');
                                if (canvasElements.length > 0) {
                                    try {
                                        return canvasElements[0].toDataURL('image/png');
                                    } catch (e) {
                                        console.log('Canvas toDataURL failed:', e);
                                    }
                                }
                                
                                // Look for SVG elements (charts might use SVG)
                                var svgElements = el.shadowRoot.querySelectorAll('svg');
                                if (svgElements.length > 0) {
                                    try {
                                        var svgData = new XMLSerializer().serializeToString(svgElements[0]);
                                        return 'data:image/svg+xml;base64,' + btoa(svgData);
                                    } catch (e) {
                                        console.log('SVG serialization failed:', e);
                                    }
                                }
                                
                                // Recursively search in nested shadow roots
                                var shadowElements = el.shadowRoot.querySelectorAll('*');
                                for (var i = 0; i < shadowElements.length; i++) {
                                    var result = findImageInShadow(shadowElements[i]);
                                    if (result) return result;
                                }
                            }
                            return null;
                        }
                        
                        return findImageInShadow(element);
                    """;

            Object result = jsExecutor.executeScript(script, element);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            System.err.println("Error finding image in shadow root: " + e.getMessage());
            return null;
        }
    }

    private byte[] downloadImageFromUrl(AndroidDriver androidDriver, String imageUrl) throws IOException {
        try {
            // Handle relative URLs
            if (imageUrl.startsWith("data:")) {
                // Handle data URLs (canvas/SVG)
                return handleDataUrl(imageUrl);
            } else if (imageUrl.startsWith("//")) {
                imageUrl = "https:" + imageUrl;
            } else if (imageUrl.startsWith("/")) {
                String currentUrl = androidDriver.getCurrentUrl();
                URL url = new URL(currentUrl);
                imageUrl = url.getProtocol() + "://" + url.getHost() + imageUrl;
            } else if (!imageUrl.startsWith("http")) {
                String currentUrl = androidDriver.getCurrentUrl();
                URL url = new URL(currentUrl);
                String baseUrl = url.getProtocol() + "://" + url.getHost() + url.getPath();
                if (!baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
                }
                imageUrl = baseUrl + imageUrl;
            }


            logger.info("Downloading image from URL: " + imageUrl);

            // Download the image
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            logger.warn("Failed to download image from URL: " + imageUrl + ", error: " + e.getMessage());
            logger.info("error : " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Failed to download image: " + e.getMessage());
        }
    }

    private static byte[] handleDataUrl(String dataUrl) throws Exception {
        try {
            // Extract base64 data from data URL
            String[] parts = dataUrl.split(",");
            if (parts.length != 2) {
                throw new Exception("Invalid data URL format");
            }

            String base64Data = parts[1];
            return Base64.getDecoder().decode(base64Data);
        } catch (Exception e) {
            System.err.println("Error processing data URL: " + e.getMessage());
            throw e;
        }
    }

}
