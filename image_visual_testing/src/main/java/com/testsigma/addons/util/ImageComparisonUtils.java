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

    public File urlToFileConverter(String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...File name:" + fileName);
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile(fileName.split("\\.")[0], "."
                        + fileName.split("\\.")[1]);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return createLocalFileFromDownloadsCopy(url, ".png");
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given file, please check the given inputs.");
        }
    }

    private File createLocalFileFromDownloadsCopy(String path, String fileFormat) throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement elem = (WebElement) js.executeScript("var input = window.document.createElement('INPUT'); " +
                "input.setAttribute('type', 'file'); " +
                "input.hidden = true; " +
                "input.onchange = function (e) { e.stopPropagation() }; " +
                "return window.document.documentElement.appendChild(input); ");

        //elem._execute('sendKeysToElement', {'value': [path ],'text':path})
        elem.sendKeys(path);
        long start = System.currentTimeMillis();
        Object result = js.executeAsyncScript("var input = arguments[0], callback = arguments[1]; " +
                        "var reader = new FileReader(); " +
                        "reader.onload = function (ev) { callback(reader.result) }; " +
                        "reader.onerror = function (ex) { callback(ex.message) }; " +
                        "reader.readAsDataURL(input.files[0]); " +
                        "input.remove(); "
                , elem);

        long end = System.currentTimeMillis();
        System.out.println("Time taken: " + (end - start));
        if (result == null || !result.toString().startsWith("data:")) {
            throw new RuntimeException("Failed to get file content: " + result);
        }
        String base64String = result.toString().substring(result.toString().indexOf("base64") + 7);
        File f = new File(path);
        String fileName = f.getName();
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        File downloadedFile = File.createTempFile(fileName, "." + fileFormat);
        // String data = new String(decodedBytes);
        System.out.println("fileName: " + fileName);
        logger.info("Local path" + downloadedFile.getAbsolutePath());
        Files.write(Paths.get(downloadedFile.getAbsolutePath()), decodedBytes);
        return downloadedFile;
    }


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

    public BufferedImage mergeImagesAndHighlightDifferences(BufferedImage baseImage, BufferedImage overlayImage,
                                                            BufferedImage combined, List<Coordinate> coordinates) {
        try {
//            logger.info("Coordinates: " + coordinates.size());
            logger.info("Coordinates: " + coordinates);
            int height = Math.max(baseImage.getHeight(), overlayImage.getHeight());
            int width = baseImage.getWidth() + overlayImage.getWidth();
            logger.info("Height: " + height + ", Width: " + width);
            if (combined == null) {
                combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            Graphics2D g2d = combined.createGraphics();
            g2d.setColor(Color.WHITE); // Optional: Set a background color
            g2d.fillRect(0, 0, width, height); // Optional: Fill the background

            g2d.drawImage(baseImage, 0, 0, null);
            g2d.drawImage(overlayImage, baseImage.getWidth(), 0, null);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)); // 0.5f for 50% transparency

            // Set the color for filling rectangles
            g2d.setColor(new Color(255, 0, 0)); // Red color

            // Iterate over the list of coordinates and fill rectangles
            for (Coordinate coordinate : coordinates) {
                g2d.fillRect(coordinate.getX(), coordinate.getY(), coordinate.getW(), coordinate.getH());
            }

            for (Coordinate coordinate : coordinates) {
                g2d.fillRect(coordinate.getX() + baseImage.getWidth(), coordinate.getY(),
                        coordinate.getW(), coordinate.getH());
            }

            g2d.dispose();
        } catch (Exception e) {
            logger.debug("Error while merging images and highlighting differences: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
        logger.info("Images merged and differences highlighted");
        return combined;
    }



}
