package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.NoSuchElementException;

@Data
@Action(actionText = "download image from the <img> tag using element element-locator and store the file path of " +
        "saved image in runtime variable variable_name",
        description = "Download image from the <img> tag and store the file path of the saved image" +
                " in a runtime variable.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB
        )
public class DownloadImageFromImgTag extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element elementLocator;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

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
            // get the src attribute to find the image URL
            String tagName = webElement.getTagName().toLowerCase();
            byte[] imageBytes;

            if ("img".equals(tagName)) {
                // If it's an img tag, get the image source and download it
                String imageSrc = webElement.getAttribute("src");
                logger.info("Found img element with src: " + imageSrc);
                imageBytes = downloadImageFromUrl(imageSrc);
            } else {
                // If it's not an img tag, take a screenshot of the element
                logger.info("Element is not an img tag, taking element screenshot");
                imageBytes = webElement.getScreenshotAs(OutputType.BYTES);
            }

            logger.info("Image captured successfully");
            saveBytesArrayToFile(file1.getAbsolutePath(), imageBytes);
            logger.info("Image saved at: " + file1.getAbsolutePath());

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(file1.getAbsolutePath());
            setSuccessMessage("Successfully downloaded the image and stored the file path in runtime variable: "
                    + variableName.getValue().toString());
        } catch (NoSuchElementException ne) {
            logger.info("Element not found: " + ExceptionUtils.getStackTrace(ne));
            setErrorMessage("Element not found: " + ne.getMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }catch (Exception e) {
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

    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try {
            // Handle relative URLs
            if (imageUrl.startsWith("//")) {
                imageUrl = "https:" + imageUrl;
            } else if (imageUrl.startsWith("/")) {
                String currentUrl = driver.getCurrentUrl();
                URL url = new URL(currentUrl);
                imageUrl = url.getProtocol() + "://" + url.getHost() + imageUrl;
            } else if (!imageUrl.startsWith("http")) {
                String currentUrl = driver.getCurrentUrl();
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
}
