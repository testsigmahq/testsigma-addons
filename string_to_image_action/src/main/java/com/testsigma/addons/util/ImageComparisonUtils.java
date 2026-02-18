package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class ImageComparisonUtils {
    WebDriver driver;
    Logger logger;

    public ImageComparisonUtils(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    RequestConfig config = RequestConfig.custom()
            .setSocketTimeout(10 * 60 * 1000)
            .setConnectionRequestTimeout(60 * 1000)
            .setConnectTimeout(60 * 1000)
            .build();

    public boolean uploadFile(String s3SignedURL, String localPath) {
        logger.debug("s3SignedURL - " + s3SignedURL);
        logger.debug("localPath - " + localPath);
        boolean localUrlExists = new File(localPath).exists();
        if (localUrlExists) {
            logger.info(String.format("Uploading test asset to storage, presigned-URL:%s, localFilePath:%s", s3SignedURL, localPath));
            try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
                HttpPut httpPut = new HttpPut(s3SignedURL);

                File file = new File(localPath);
                HttpEntity entity = EntityBuilder.create().setFile(file).build();
                httpPut.setEntity(entity);
                HttpResponse response = httpclient.execute(httpPut);
                logger.info("Upload completed");
                return true;
            } catch (Exception e) {
                logger.info("Exception while uploading custom screenshot to s3: " + ExceptionUtils.getStackTrace(e));
                return false;
            }
        } else {
            logger.info("Local path does not exist");
            return false;
        }
    }

}
