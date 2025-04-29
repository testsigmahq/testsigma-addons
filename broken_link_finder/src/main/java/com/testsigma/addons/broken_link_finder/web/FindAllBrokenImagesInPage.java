package com.testsigma.addons.broken_link_finder.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Find all broken images in url",
        description = "This action returns a list of all broken images in the given application URL",
        applicationType = ApplicationType.WEB)
public class FindAllBrokenImagesInPage extends WebAction {

    @TestData(reference = "url")
    private com.testsigma.sdk.TestData URL;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            driver.get(URL.getValue().toString());
            driver.manage().window().maximize();

            logger.info("Navigated to URL: " + URL.getValue().toString());

            List<String> brokenImages = new ArrayList<>();
            List<WebElement> imageList = new ArrayList<>();

            try {
                imageList = driver.findElements(By.tagName("img"));
                logger.info("Total images found on the page: " + imageList.size());

                // Timeout configuration
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(60000)
                        .setConnectionRequestTimeout(60000)
                        .setSocketTimeout(60000)
                        .build();

                for (WebElement img : imageList) {
                    if (img != null) {
                        String src = img.getAttribute("src");
                        if (src != null) {
                            try {
                                // Check if image is actually loaded
                                boolean isLoaded = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                                        .executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0", img);

                                if (!isLoaded) {
                                    // If image is not loaded, try HTTP check
                                    HttpClient client = HttpClientBuilder.create()
                                            .setDefaultRequestConfig(requestConfig)
                                            .build();
                                    HttpGet request = new HttpGet(src);
                                    request.setHeader("User-Agent", "Mozilla/5.0");
                                    request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                                    request.setHeader("Accept-Language", "en-US,en;q=0.5");
                                    request.setHeader("Connection", "keep-alive");

                                    long startTime = System.currentTimeMillis();
                                    HttpResponse response = client.execute(request);
                                    long endTime = System.currentTimeMillis();

                                    logger.info("Checked image URL: " + src + " | Response code: " +
                                            response.getStatusLine().getStatusCode() + " | Time taken: " + (endTime - startTime) + " ms");

                                    if (response.getStatusLine().getStatusCode() != 200) {
                                        logger.warn("Image with src " + src + " failed to load and returned non-200 status code");
                                        brokenImages.add(src);
                                    }
                                } else {
                                    logger.info("Image " + src + " is loaded successfully");
                                }
                            } catch (Exception e) {
                                logger.warn("Error checking image URL: " + src);
                                logger.warn(e.getMessage());
                                // Only add to broken images if it's a timeout or connection error
                                if (e.getMessage().contains("timeout") || e.getMessage().contains("connection")) {
                                    brokenImages.add(src);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Error while processing images: " + e.getMessage());
            }

            logger.info("Finished checking images. Total: " + imageList.size() + ", Broken: " + brokenImages.size());

            if (!brokenImages.isEmpty()) {
                setSuccessMessage("Broken Images [" + brokenImages.size() + "]: " + brokenImages);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("There are no Broken Images in the page");
                return Result.SUCCESS;
            }

        } catch (Exception exception) {
            logger.warn("Exception occurred: " + ExceptionUtils.getStackTrace(exception));
            setErrorMessage("Exception: " + ExceptionUtils.getStackTrace(exception));
            return Result.FAILED;
        }
    }
}