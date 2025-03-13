package com.testsigma.addons.web.util;

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
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class PDFUtils {
    WebDriver driver;
    Logger logger;

    public PDFUtils(WebDriver driver, Logger logger) {
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
                return new File(url);
            }
        } catch (Exception e) {
//            setErrorMessage("Unable to access the given pdfs, please check the given inputs.");
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given pdfs, please check the given inputs.");
        }
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


    public int getPdfPageCount(File pdfFile) {
        try {
            PDDocument document = Loader.loadPDF(pdfFile);
            int numberOfPagesInPdf = document.getNumberOfPages();
            document.close();
            return numberOfPagesInPdf;
        } catch (IOException e) {
            logger.info("Exception while getting the number of pages in the pdf: " + ExceptionUtils.getStackTrace(e));
            return -1;
        }
    }


    public void pdfToImage(String pdfFilePath, String imageOutputDir, String type, int index) {
        try {
            logger.info(String.format("Converting page %d in pdf to image and storing in directory %s",
                    index, imageOutputDir));

            // Handle S3 URL by downloading to a temporary file first
            File pdfFile;
            if (pdfFilePath.startsWith("http://") || pdfFilePath.startsWith("https://")) {
                String tempFileName = "temp_" + System.currentTimeMillis() + ".pdf";
                pdfFile = downloadFromUrl(pdfFilePath, tempFileName);
                logger.info("Downloaded S3 URL to temporary file: " + pdfFile.getAbsolutePath());
            } else {
                // This is a local file path
                pdfFile = new File(pdfFilePath);
            }

            // Now process the PDF file
            PDDocument document = Loader.loadPDF(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(index - 1, 300);
            ImageIOUtil.writeImage(bim, String.format("%s/%s_page_%d.png", imageOutputDir, type, index), 300);
            document.close();

            // Clean up temp file if we created one
            if (pdfFilePath.startsWith("http")) {
                boolean deleted = pdfFile.delete();
                if (deleted) {
                    logger.info("Temporary PDF file deleted successfully");
                }
            }

            logger.info("Pdf to image conversion successful for page " + index);
        } catch (IOException e) {
            String message = "Unable to convert pdf into image pages: " + e.getMessage();
            logger.info(String.format("Exception while converting pdf %s into pages: %s", pdfFilePath,
                    ExceptionUtils.getStackTrace(e)));
            throw new RuntimeException(message);
        }
    }

    // Helper method to download from URL to a local file
    private File downloadFromUrl(String url, String fileName) throws IOException {
        File tempFile = File.createTempFile(fileName, ".pdf");
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }


}
