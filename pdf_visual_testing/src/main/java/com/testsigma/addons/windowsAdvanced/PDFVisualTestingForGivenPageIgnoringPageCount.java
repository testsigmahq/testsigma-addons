package com.testsigma.addons.windowsAdvanced;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.web.util.Constants;
import com.testsigma.addons.web.util.ResponseObject;
import com.testsigma.addons.windowsAdvanced.util.PDFUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.openqa.selenium.NoSuchElementException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Data
@Action(actionText = "Verify that the base pdf base-pdf-file-path and the actual pdf actual-pdf-file-path is" +
        " same for page page-number by performing visual analysis (ignoring page count)",
        description = "Verifies that the base pdf and the actual pdf is same by performing visual" +
                " analysis. (indexing starts from 1)",
        displayName = "PDF Visual Testing For Given Page Ignoring Page Count",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = true)
public class PDFVisualTestingForGivenPageIgnoringPageCount extends WindowsAdvancedAction {

    @TestData(reference = "base-pdf-file-path")
    private com.testsigma.sdk.TestData basePdfPath_;

    @TestData(reference = "actual-pdf-file-path")
    private com.testsigma.sdk.TestData actualPdfPath_;

    @TestData(reference = "page-number")
    private com.testsigma.sdk.TestData pageNumber_;

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
        PDFUtils pdfUtils = new PDFUtils(logger);

        try {
            // Use StringBuilder to capture error messages
            StringBuilder errorMessageBuilder = new StringBuilder();
            result = checkBaseConditions(basePdfPath, actualPdfPath, pdfUtils, errorMessageBuilder);
            if (result != Result.SUCCESS) {
                setErrorMessage(errorMessageBuilder.toString());
                return result;
            }

            int page = Integer.parseInt(pageNumber_.getValue().toString());
            result = performVisualTesting(basePdfPath, actualPdfPath, page, pdfUtils, errorMessageBuilder);
            if (result != Result.SUCCESS) {
                setErrorMessage(errorMessageBuilder.toString());
            }
            setSuccessMessage("Successfully verified that the base pdf and actual pdf is same by visual testing. " +
                    "(Note: Step screenshot contains the image of actual PDF)");
        } catch (RuntimeException e) {
            logger.info("Runtime Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform the operation : " + e.getMessage());
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform the operation : " + e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }

    private Result checkBaseConditions(String basePdfPath, String actualPdfPath, PDFUtils pdfUtils,
                                       StringBuilder errorMessageBuilder) {
        try {
            File basePDF = pdfUtils.urlToFileConverter("base.pdf", basePdfPath);
            File actualPDF = pdfUtils.urlToFileConverter("actual.pdf", actualPdfPath);

            if (!basePDF.getName().endsWith(".pdf") && !actualPDF.getName().endsWith(".pdf")) {
                String message = "Unsupported file types give only pdf files as input, screenshot" +
                        " might not be displayed";
                errorMessageBuilder.append(message);
                return Result.FAILURE;
            }

            if (!basePDF.exists()) {
                String message = "Base PDF does not exist : " + basePDF.getAbsolutePath() +
                        ", please given valid file input, screenshot might not be displayed";
                errorMessageBuilder.append(message);
                return Result.FAILURE;
            }

            if (!actualPDF.exists()) {
                String message = "Actual PDF does not exist : " + actualPDF.getAbsolutePath() +
                        ", please given valid file input. (Note: screenshot is not displayed)";
                errorMessageBuilder.append(message);
                return Result.FAILURE;
            }

            int basePdfPageCount = pdfUtils.getPdfPageCount(basePDF);
            int actualPdfPageCount = pdfUtils.getPdfPageCount(actualPDF);

            /*if (basePdfPageCount != actualPdfPageCount) {
                String message = "Number of pages in the base pdf and actual pdf are not same, " +
                        "Base pdf page count = <b>" + basePdfPageCount + "</b>, Actual pdf page count = <b>"
                        + actualPdfPageCount + "</b>";
                errorMessageBuilder.append(message);
                return Result.FAILED;
            }*/

            int page = Integer.parseInt(pageNumber_.getValue().toString());
            if (page > basePdfPageCount ) {
                logger.info(String.format("page = %s, base pdf page count = %s", page, actualPdfPageCount));
                String message = String.format("given page number is greater than the number of pages in base pdf. " +
                                "page number = <b>%s</b> and base pdf page count = <b>%s</b>. (Note: screenshot is not displayed)",
                        page, basePdfPageCount);
                errorMessageBuilder.append(message);
                return Result.FAILED;
            } else if (page > actualPdfPageCount) {
                logger.info(String.format("page = %s, actual pdf page count = %s", page, actualPdfPageCount));
                String message = String.format("given page number is greater than the number of pages in actual pdf. " +
                                "page number = <b>%s</b> and actual pdf page count = <b>%s</b>. (Note: screenshot is not displayed)",
                        page, actualPdfPageCount);
                errorMessageBuilder.append(message);
                return Result.FAILED;
            }
        } catch (ParseException e) {
            String message = "Unsupported input for page-number, please provide a valid integer value " +
                    "(Note: screenshot is not displayed)";
            errorMessageBuilder.append(message);
            return Result.FAILURE;
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            String message = "Unable to perform the operation : " + e.getMessage();
            errorMessageBuilder.append(message);
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    private Result performVisualTesting(String basePdfPath, String actualPdfPath, int page, PDFUtils pdfUtils,
                                        StringBuilder errorMessageBuilder) {
        try {
            logger.info("Creating necessary temp directories and files...");
            String basePdfDirectoryPath = String.valueOf(Files.createTempDirectory("basePdfDirectory"));
            String actualPdfDirectoryPath = String.valueOf(Files.createTempDirectory("actualPdfDirectory"));
            File combinedImage = File.createTempFile("combined", ".png");
            logger.info("Converting pages of both pdfs to images");
            pdfUtils.pdfToImage(basePdfPath, basePdfDirectoryPath, "input1", page);
            pdfUtils.pdfToImage(actualPdfPath, actualPdfDirectoryPath, "input2", page);
            logger.info("Converted both pdf pages into images");

            logger.info("Validating the pages of images at both directories");
            File[] basePdfDirImages = new File(basePdfDirectoryPath).listFiles();
            File[] actualPdfDirImages = new File(actualPdfDirectoryPath).listFiles();
            if (basePdfDirImages == null || actualPdfDirImages == null) {
                String message = "Unable to retrieve pages from the pdfs, make sure both pdfs " +
                        "are not empty and accessible";
                errorMessageBuilder.append(message);
                return Result.FAILURE;
            }
            logger.info("Initiating visual testing");
            boolean compareResult = true;
            BufferedImage combined = null;

            File file1 = new File(basePdfDirectoryPath + File.separator + "input1_page_" + page + ".png");
            File file2 = new File(actualPdfDirectoryPath + File.separator + "input2_page_" + page + ".png");
            if (file1.exists() && file2.exists()) {
                BufferedImage baseImage = ImageIO.read(file1);
                BufferedImage actualImage = ImageIO.read(file2);

                boolean apiResult = performApiCall(file1, file2, page, errorMessageBuilder);
                if (!apiResult) {
                    compareResult = false;
                    logger.info("dissimilarities found in the pdfs, creating side-by-side image");
                    logger.info("responseObject " + responseObject.getDiff_coordinates());
                    logger.info("responseObject " + responseObject);
                    combined = pdfUtils.mergeImagesAndHighlightDifferences(baseImage, actualImage,
                            combined, responseObject.getDiff_coordinates());
                    logger.info("Created side-by-side image");
                }
            }
            return uploadScreenshot(compareResult, actualPdfDirImages, combined, combinedImage, errorMessageBuilder);
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            String message = "Unable to perform the operation during visual testing: " + e.getMessage();
            errorMessageBuilder.append(message);
            return Result.FAILED;
        }
    }

    private Result uploadScreenshot(boolean compareResult, File[] actualPdfDirImages, BufferedImage combined,
                                    File combinedImage, StringBuilder errorMessageBuilder) {
        try {
            PDFUtils pdfUtils = new PDFUtils(logger);
            String s3Url = testStepResult.getScreenshotUrl();
            if (compareResult) {
                boolean uploadS3Result = pdfUtils.uploadFile(s3Url, actualPdfDirImages[0].getAbsolutePath());
                if (!uploadS3Result) {
                    logger.info("Error occurred while uploading combined image to s3," +
                            " screenshot might not be displayed");
                }
                logger.info("Upload complete.");
                setSuccessMessage("Successfully verified that the base pdf and actual pdf is same by visual testing." +
                        " (Note: Step screenshot contains the image of actual PDF)");
            } else {
                ImageIO.write(combined, "png", combinedImage);
                boolean uploadS3Result = pdfUtils.uploadFile(s3Url, combinedImage.getAbsolutePath());
                if (!uploadS3Result) {
                    logger.debug("Error occurred while uploading combined image to S3," +
                            " screenshot might not be displayed");
                }
                String message = "Comparison failed because visual testing detected dissimilarities," +
                        " check step screenshot for visual results";
                errorMessageBuilder.append(message);
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            String message = "Unable to perform the operation during screenshot upload: " + e.getMessage();
            errorMessageBuilder.append(message);
            return Result.FAILED;
        }
        return Result.SUCCESS;
    }

    public boolean performApiCall(File baseImage, File actualImage, int page, StringBuilder errorMessageBuilder) {
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
                    errorMessageBuilder.append(String.format("Visual testing failed at <b>page %s</b> no response" +
                            " body present in the visual test response", page));
                    throw new RuntimeException("Visual testing failed with no response body");
                }
            } else {
                errorMessageBuilder.append(String.format("Visual testing failed at <b>page %s</b> error occurred internally",
                        page));
                throw new RuntimeException("Visual testing failed with internal server error");
            }
        } catch (IOException e) {
            errorMessageBuilder.append(String.format("Exception occurred while performing visual test at page %s : %s",
                    page, ExceptionUtils.getStackTrace(e)));
            logger.info(String.format("Exception occurred while performing visual test at page %s : %s", page,
                    ExceptionUtils.getStackTrace(e)));
            throw new RuntimeException("Error occurred while performing visual test at page " + page);
        }
    }

}
