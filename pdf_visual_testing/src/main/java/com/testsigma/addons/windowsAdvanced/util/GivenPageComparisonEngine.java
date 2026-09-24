package com.testsigma.addons.windowsAdvanced.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.web.util.Constants;
import com.testsigma.addons.web.util.ResponseObject;
import com.testsigma.sdk.Logger;
import com.testsigma.sdk.Result;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Shared implementation behind "PDF Visual Testing For Given Page" and its "...Ignoring Page
 * Count" counterpart, which otherwise differed only in whether a base/actual page-count mismatch
 * is treated as an immediate failure. {@code enforcePageCountMatch} carries that one difference;
 * every other action stays a thin execute() wrapper around this engine.
 */
public class GivenPageComparisonEngine {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(8, 5, TimeUnit.MINUTES))
            .connectTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(120))
            .writeTimeout(Duration.ofSeconds(120))
            .callTimeout(Duration.ofSeconds(180))
            .build();

    private final Logger logger;
    private final PDFUtils pdfUtils;

    public GivenPageComparisonEngine(Logger logger) {
        this.logger = logger;
        this.pdfUtils = new PDFUtils(logger);
    }

    public static class Outcome {
        public final Result result;
        public final String message;

        Outcome(Result result, String message) {
            this.result = result;
            this.message = message;
        }
    }

    public Outcome compareGivenPage(String basePdfPath, String actualPdfPath, String pageNumberRaw,
                                     boolean enforcePageCountMatch, String screenshotUploadUrl) {
        int page;
        try {
            page = Integer.parseInt(pageNumberRaw);
        } catch (NumberFormatException e) {
            return new Outcome(Result.FAILURE, "Unsupported input for page-number, please provide a valid" +
                    " integer value (Note: screenshot is not displayed)");
        }
        if (page < 1) {
            return new Outcome(Result.FAILURE, "page-number must be 1 or greater, given value = <b>" + page +
                    "</b> (Note: screenshot is not displayed)");
        }

        StringBuilder errorMessageBuilder = new StringBuilder();
        Result conditionResult = checkBaseConditions(basePdfPath, actualPdfPath, page, enforcePageCountMatch,
                errorMessageBuilder);
        if (conditionResult != Result.SUCCESS) {
            return new Outcome(conditionResult, errorMessageBuilder.toString());
        }

        Result visualResult = performVisualTesting(basePdfPath, actualPdfPath, page, screenshotUploadUrl,
                errorMessageBuilder);
        if (visualResult != Result.SUCCESS) {
            return new Outcome(visualResult, errorMessageBuilder.toString());
        }
        return new Outcome(Result.SUCCESS, "Successfully verified that the base pdf and actual pdf is same by" +
                " visual testing. (Note: Step screenshot contains the image of actual PDF)");
    }

    private Result checkBaseConditions(String basePdfPath, String actualPdfPath, int page,
                                        boolean enforcePageCountMatch, StringBuilder errorMessageBuilder) {
        try {
            File basePDF = pdfUtils.urlToFileConverter("base.pdf", basePdfPath);
            File actualPDF = pdfUtils.urlToFileConverter("actual.pdf", actualPdfPath);

            if (!basePDF.getName().endsWith(".pdf") || !actualPDF.getName().endsWith(".pdf")) {
                errorMessageBuilder.append("Unsupported file types give only pdf files as input, screenshot" +
                        " might not be displayed");
                return Result.FAILURE;
            }

            if (!basePDF.exists()) {
                errorMessageBuilder.append("Base PDF does not exist : " + basePDF.getAbsolutePath() +
                        ", please given valid file input, screenshot might not be displayed");
                return Result.FAILURE;
            }

            if (!actualPDF.exists()) {
                errorMessageBuilder.append("Actual PDF does not exist : " + actualPDF.getAbsolutePath() +
                        ", please given valid file input. (Note: screenshot is not displayed)");
                return Result.FAILURE;
            }

            int basePdfPageCount = pdfUtils.getPdfPageCount(basePDF);
            int actualPdfPageCount = pdfUtils.getPdfPageCount(actualPDF);

            if (enforcePageCountMatch && basePdfPageCount != actualPdfPageCount) {
                errorMessageBuilder.append("Number of pages in the base pdf and actual pdf are not same, " +
                        "Base pdf page count = <b>" + basePdfPageCount + "</b>, Actual pdf page count = <b>"
                        + actualPdfPageCount + "</b>");
                return Result.FAILED;
            }

            if (page > basePdfPageCount) {
                logger.info(String.format("page = %s, base pdf page count = %s", page, basePdfPageCount));
                errorMessageBuilder.append(String.format("given page number is greater than the number of pages" +
                                " in base pdf. page number = <b>%s</b> and base pdf page count = <b>%s</b>." +
                                " (Note: screenshot is not displayed)", page, basePdfPageCount));
                return Result.FAILED;
            }
            if (page > actualPdfPageCount) {
                logger.info(String.format("page = %s, actual pdf page count = %s", page, actualPdfPageCount));
                errorMessageBuilder.append(String.format("given page number is greater than the number of pages" +
                                " in actual pdf. page number = <b>%s</b> and actual pdf page count = <b>%s</b>." +
                                " (Note: screenshot is not displayed)", page, actualPdfPageCount));
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            errorMessageBuilder.append("Unable to perform the operation : " + e.getMessage());
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    private Result performVisualTesting(String basePdfPath, String actualPdfPath, int page,
                                         String screenshotUploadUrl, StringBuilder errorMessageBuilder) {
        try {
            logger.info("Creating necessary temp directories and files...");
            String basePdfDirectoryPath = String.valueOf(Files.createTempDirectory("basePdfDirectory"));
            String actualPdfDirectoryPath = String.valueOf(Files.createTempDirectory("actualPdfDirectory"));
            logger.info("Converting pages of both pdfs to images");
            pdfUtils.pdfToImage(basePdfPath, basePdfDirectoryPath, "input1", page);
            pdfUtils.pdfToImage(actualPdfPath, actualPdfDirectoryPath, "input2", page);
            logger.info("Converted both pdf pages into images");

            File file1 = new File(basePdfDirectoryPath + File.separator + "input1_page_" + page + ".png");
            File file2 = new File(actualPdfDirectoryPath + File.separator + "input2_page_" + page + ".png");
            if (!file1.exists() || !file2.exists()) {
                errorMessageBuilder.append(String.format("Unable to render page %d from the given pdfs." +
                        " (Note: screenshot is not displayed)", page));
                return Result.FAILED;
            }

            BufferedImage baseImage = ImageIO.read(file1);
            BufferedImage actualImage = ImageIO.read(file2);

            ApiCallResult apiCallResult = performApiCall(file1, file2, page);
            if (apiCallResult.responseObject == null) {
                errorMessageBuilder.append(apiCallResult.errorMessage);
                return Result.FAILED;
            }

            boolean pagesMatch = apiCallResult.responseObject.getPer_similar() == 1
                    && apiCallResult.responseObject.getDiff_coordinates().isEmpty();

            if (pagesMatch) {
                return uploadScreenshot(true, file2, null, screenshotUploadUrl, errorMessageBuilder);
            } else {
                logger.info("dissimilarities found in the pdfs, creating side-by-side image");
                BufferedImage combined = pdfUtils.mergeImagesAndHighlightDifferences(baseImage, actualImage,
                        null, apiCallResult.responseObject.getDiff_coordinates());
                return uploadScreenshot(false, file2, combined, screenshotUploadUrl, errorMessageBuilder);
            }
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            errorMessageBuilder.append("Unable to perform the operation during visual testing: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private Result uploadScreenshot(boolean pagesMatch, File actualPageImage, BufferedImage combined,
                                      String screenshotUploadUrl, StringBuilder errorMessageBuilder) {
        try {
            if (pagesMatch) {
                boolean uploadS3Result = pdfUtils.uploadFile(screenshotUploadUrl, actualPageImage.getAbsolutePath());
                if (!uploadS3Result) {
                    logger.info("Error occurred while uploading screenshot to s3, screenshot might not be displayed");
                }
                return Result.SUCCESS;
            } else {
                File combinedImage = File.createTempFile("combined", ".png");
                ImageIO.write(combined, "png", combinedImage);
                boolean uploadS3Result = pdfUtils.uploadFile(screenshotUploadUrl, combinedImage.getAbsolutePath());
                if (!uploadS3Result) {
                    logger.debug("Error occurred while uploading combined image to S3, screenshot might not be displayed");
                }
                errorMessageBuilder.append("Comparison failed because visual testing detected dissimilarities," +
                        " check step screenshot for visual results");
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            errorMessageBuilder.append("Unable to perform the operation during screenshot upload: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private static class ApiCallResult {
        final ResponseObject responseObject;
        final String errorMessage;

        ApiCallResult(ResponseObject responseObject, String errorMessage) {
            this.responseObject = responseObject;
            this.errorMessage = errorMessage;
        }
    }

    private ApiCallResult performApiCall(File baseImage, File actualImage, int page) {
        try {
            logger.info(String.format("Performing visual testing for %s and %s", baseImage.getAbsolutePath(),
                    actualImage.getAbsolutePath()));
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
            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_API_END_POINT)
                    .method("POST", builder.build())
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        logger.info(String.format("Response body for page %s testing: %s", page, responseBody));
                        ResponseObject responseObject = mapper.readValue(responseBody, ResponseObject.class);
                        return new ApiCallResult(responseObject, null);
                    } else {
                        return new ApiCallResult(null, String.format("Visual testing failed at <b>page %s</b>" +
                                " no response body present in the visual test response", page));
                    }
                } else {
                    return new ApiCallResult(null, String.format("Visual testing failed at <b>page %s</b> error" +
                            " occurred internally", page));
                }
            }
        } catch (IOException e) {
            logger.info(String.format("Exception occurred while performing visual test at page %s : %s", page,
                    ExceptionUtils.getStackTrace(e)));
            return new ApiCallResult(null, String.format("Exception occurred while performing visual test at" +
                    " page %s : %s", page, e.getMessage()));
        }
    }
}
