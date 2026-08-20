package com.testsigma.addons.windowsAdvanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.web.util.Constants;
import com.testsigma.addons.web.util.Coordinate;
import com.testsigma.addons.web.util.ResponseObject;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import okhttp3.*;
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
import org.openqa.selenium.NoSuchElementException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;


@Data
@Action(actionText = "Verify that the base pdf base-pdf-file-path and the actual pdf actual-pdf-file-path is" +
        " same by performing visual analysis",
        description = "Verifies that the base pdf and the actual pdf is same by performing visual" +
                " analysis",
        displayName = "PDF Visual Testing",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = true)
public class PDFVisualTesting extends WindowsAdvancedAction {


    @TestData(reference = "base-pdf-file-path")
    private com.testsigma.sdk.TestData basePdfPath_;

    @TestData(reference = "actual-pdf-file-path")
    private com.testsigma.sdk.TestData actualPdfPath_;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;


    RequestConfig config = RequestConfig.custom()
            .setSocketTimeout(10 * 60 * 1000)
            .setConnectionRequestTimeout(60 * 1000)
            .setConnectTimeout(60 * 1000)
            .build();
    ObjectMapper mapper = new ObjectMapper();
    ResponseObject responseObject = new ResponseObject();

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        String basePdfPath = basePdfPath_.getValue().toString();
        String actualPdfPath = actualPdfPath_.getValue().toString();

        int page;
        try {
            File basePDF = urlToFileConverter("base.pdf", basePdfPath);
            File actualPDF = urlToFileConverter("actual.pdf", actualPdfPath);

            if (!basePDF.getName().endsWith(".pdf") && !actualPDF.getName().endsWith(".pdf")) {
                setErrorMessage("Unsupported file types give only pdf files as input");
                throw new RuntimeException("Unsupported file types");
            }

            if (!basePDF.exists()) {
                setErrorMessage("Base PDF does not exist : " + basePDF.getAbsolutePath() +
                        ", please given valid file input");
                throw new RuntimeException("Base PDF not found");
            }

            if (!actualPDF.exists()) {
                setErrorMessage("Actual PDF does not exist : " + basePDF.getAbsolutePath() +
                        ", please given valid file input");
                throw new RuntimeException("Actual PDF not found");
            }

            logger.info("Creating necessary temp directories and files...");
            String basePdfDirectoryPath = String.valueOf(Files.createTempDirectory("basePdfDirectory"));
            String actualPdfDirectoryPath = String.valueOf(Files.createTempDirectory("actualPdfDirectory"));
            File combinedImage = File.createTempFile("combined",".png");
            logger.info("Created.");


            logger.info("Converting pages of both pdfs to images");
            pdfToImages(basePDF.getAbsolutePath(), basePdfDirectoryPath, "input1");
            pdfToImages(actualPDF.getAbsolutePath(), actualPdfDirectoryPath, "input2");
            logger.info("Converted both pdf pages into images");

            logger.info("Validating the pages of images at both directories");
            File[] basePdfDirImages = new File(basePdfDirectoryPath).listFiles();
            File[] actualPdfDirImages = new File(actualPdfDirectoryPath).listFiles();
            if (basePdfDirImages == null || actualPdfDirImages == null) {
                setErrorMessage("Unable to retrieve pages from the pdfs, make sure both pdfs are not empty and accessible");
                throw new RuntimeException("Unable retrieve pages from pdfs");
            }
            if (basePdfDirImages.length != actualPdfDirImages.length) {
                setErrorMessage(String.format("Unequal page counts - pages in base pdf are %s, pages in actual " +
                        "pdf are %s", basePdfDirImages.length, actualPdfDirImages.length));
                throw new RuntimeException("Unequal page count");
            }
            logger.info("No errors in the directories");
            logger.info("Initiating visual testing");
            boolean compareResult = true;
            BufferedImage combined = null;

            for (page = 1; page <= actualPdfDirImages.length; page++) {
                File file1 = new File(basePdfDirectoryPath + File.separator + "input1_page_" + page + ".png");
                File file2 = new File(actualPdfDirectoryPath + File.separator + "input2_page_" + page + ".png");
                if (file1.exists() && file2.exists()) {
                    BufferedImage baseImage = ImageIO.read(file1);
                    BufferedImage actualImage = ImageIO.read(file2);

                    boolean apiResult = performApiCall(file1, file2, page);
                    if (!apiResult) {
                        compareResult = false;
                        combined = mergeImagesAndHighlightDifferences(baseImage , actualImage, combined, responseObject.getDiff_coordinates());
                    }
                }
            }
            String s3Url = testStepResult.getScreenshotUrl();

            // if there are no changes in the pdf, uploading the first page of the actualPdf
            if (compareResult) {
                boolean uploadS3Result = uploadFile(s3Url, actualPdfDirImages[0].getAbsolutePath());
                if (!uploadS3Result) {
                    logger.info("Error occurred while uploading combined image to s3, screenshot might not be displayed");
                }
                System.out.println("Upload complete.");
                setSuccessMessage("Successfully verified that the base pdf and actual pdf is same by visual testing. (Note: Step screenshot contains the image of actual PDF)");
            } else {
                // uploading the screenshot of the image where we encounter the difference (side-by-side).
                ImageIO.write(combined, "png", combinedImage);
                boolean uploadS3Result = uploadFile(s3Url, combinedImage.getAbsolutePath());
                if (!uploadS3Result) {
                    logger.info("Error occurred while uploading combined image to S3, screenshot might not be displayed");
                }
                result = Result.FAILED;
                setErrorMessage("Comparison failed because visual testing detected dissimilarities," +
                        " check step screenshot for visual results");
            }
        } catch (RuntimeException e) {
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform the operation");
            result = Result.FAILED;
        }
        return result;
    }


    public File urlToFileConverter(String fileName, String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...File name:" + fileName);
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName() + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
            }
        } catch (Exception e) {
            setErrorMessage("Unable to access the given pdfs, please check the given inputs.");
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access pdfs");
        }
    }

    public void pdfToImages(String pdfFilePath, String imageOutputDir, String type) {
        try {
            logger.info(String.format("Converting every page in pdf at %s path to image and storing those images" +
                    " in directory %s", pdfFilePath, imageOutputDir));
            PDDocument document = Loader.loadPDF(new File(pdfFilePath));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                ImageIOUtil.writeImage(bim, String.format("%s/%s_page_%d.png", imageOutputDir, type, page + 1), 300);
            }
            document.close();
            logger.info("Pdf to image conversion successful for the pdf " + pdfFilePath);
        } catch (IOException e) {
            String message = "Unable to convert pdf into image pages";
            logger.info(String.format("Exception while converting pdf %s into pages: %s", pdfFilePath,
                    ExceptionUtils.getStackTrace(e)));
            setErrorMessage(message);
            throw new RuntimeException(message);
        }
    }

    public boolean performApiCall(File baseImage, File actualImage, int page) {
        try {
            logger.info(String.format("Performing visual testing for %s and %s", baseImage.getAbsolutePath(),
                    actualImage.getAbsolutePath()));
            OkHttpClient client = new OkHttpClient();
            logger.info("Initiating http client");
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("action", "COMPARE")
                    .addFormDataPart("scalingType", "BASE_IMAGE_SCALING")
                    .addFormDataPart(
                            "baseImageFile",
                            baseImage.getName(),
                            RequestBody.create(MediaType.parse("image/png"), baseImage)
                    )
                    .addFormDataPart(
                            "actualImageFile",
                            actualImage.getName(),
                            RequestBody.create(MediaType.parse("image/png"), actualImage)
                    );
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_API_END_POINT)
                    .method("POST", requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();
            logger.info("Making api call to visual server");
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                logger.info("Response is successful");
                if (response.body() != null) {
                    logger.info("Response body received");
                    String responseBody = response.body().string();
                    logger.info(String.format("Response body for page %s testing: %s", page, responseBody));
                    responseObject = mapper.readValue(responseBody, ResponseObject.class);
                    logger.info("Deserialized the response body");
                    double percentage = responseObject.getPer_similar();
                    logger.info("Percentage similarity: " + percentage * 100);
                    return percentage == 1 && responseObject.getDiff_coordinates().isEmpty();
                } else {
                    setErrorMessage(String.format("Visual testing failed at <b>page %s</b> no response body " +
                            "present in the visual test response", page));
                    throw new RuntimeException("Visual testing failed with no response body");
                }
            } else {
                setErrorMessage(String.format("Visual testing failed at <b>page %s</b> error occurred internally",
                        page));
                throw new RuntimeException("Visual testing failed with internal server error");
            }
        } catch (IOException e) {

            logger.info(String.format("Exception occurred while performing visual test at page %s : %s", page,
                    ExceptionUtils.getStackTrace(e)));
            setErrorMessage(String.format("Unable to perform visual testing for : <b>page %s</b>", page));
            throw new RuntimeException("Error occurred while performing visual test at page " + page);
        }
    }

    private boolean uploadFile(String s3SignedURL, String localPath) {
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
                logger.info("Response from s3: " + response.getStatusLine().getStatusCode());
                logger.info("Upload completed");
                return true;
            } catch (Exception e) {
                logger.info("Exception while uploading custom screenshot to s3: "+ExceptionUtils.getStackTrace(e));
                return false;
            }
        }
        else {
            logger.info("Local path does not exist");
            return false;
        }
    }

    private BufferedImage mergeImagesAndHighlightDifferences(BufferedImage baseImage, BufferedImage overlayImage, BufferedImage combined, List<Coordinate> coordinates) {
        int height = Math.max(baseImage.getHeight(), overlayImage.getHeight());
        int width = baseImage.getWidth() + overlayImage.getWidth();

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
            g2d.fillRect(coordinate.getX() + baseImage.getWidth(), coordinate.getY(), coordinate.getW(), coordinate.getH());
        }

        g2d.dispose();

        return combined;
    }


}
