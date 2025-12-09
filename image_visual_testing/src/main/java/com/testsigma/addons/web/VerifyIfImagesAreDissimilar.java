package com.testsigma.addons.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.Constants;
import com.testsigma.addons.util.ImageComparisonUtils;
import com.testsigma.addons.util.ResponseObject;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Data
@Action(actionText = "Verify if image actual-image is dissimilar to base-image",
        description = "This action compares two images using visual testing and verifies they are dissimilar without any threshold consideration.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB,
        useCustomScreenshot = true)
public class VerifyIfImagesAreDissimilar extends WebAction {

    @TestData(reference = "actual-image")
    private com.testsigma.sdk.TestData image1;

    @TestData(reference = "base-image")
    private com.testsigma.sdk.TestData image2;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    RequestConfig config = RequestConfig.custom()
            .setSocketTimeout(10 * 60 * 1000)
            .setConnectionRequestTimeout(60 * 1000)
            .setConnectTimeout(60 * 1000)
            .build();
    ObjectMapper mapper = new ObjectMapper();
    ResponseObject responseObject = new ResponseObject();

    @Override
    public Result execute() {
        StringBuilder errorMessageBuilder = new StringBuilder();

        // implementation for comparing two images using visual testing goes here
        logger.info("Comparing images: " + image1.getValue().toString() + " and " + image2.getValue().toString());
        String baseImagePath = image1.getValue().toString();
        String actualImagePath = image2.getValue().toString();

        BufferedImage combined = null;

        // create a temp file
        try {
            logger.info("initializing image comparison utils");
            ImageComparisonUtils imageComparisonUtils = new ImageComparisonUtils(driver, logger);
            BufferedImage baseImage = null;
            BufferedImage actualImage = null;
            File file1 = urlToFileConverter("first_image", baseImagePath);
            File file2 = urlToFileConverter("second_image", actualImagePath);
            logger.info("Base image file path: " + file1.getAbsolutePath());
            baseImage = ImageIO.read(file1);
            actualImage = ImageIO.read(file2);
            logger.info("Actual image file path: " + file2.getAbsolutePath());

//            logger.info("Base image dimensions: " + baseImage.getWidth() + "x" + baseImage.getHeight());
            double percentageSimilarity = performApiCall(file1, file2);
            if (responseObject.getDiff_coordinates() == null) {
                logger.info("Diff coordinates are null, initializing to empty list");
                responseObject.setDiff_coordinates(List.of());
            }

            // Check if images are dissimilar (similarity < 100%)
            if ((percentageSimilarity * 100) < 100.0) {
                logger.info("Images are dissimilar, similarity percentage " +
                        (percentageSimilarity * 100) + "%");
                uploadScreenshot(true, file2, actualImage, null, errorMessageBuilder);
                setSuccessMessage("Successfully verified that the base image and actual image are dissimilar. " +
                        "Dissimilarity percentage: " + ((100) - percentageSimilarity * 100) + "%");
                return Result.SUCCESS;
            }

            // Images are identical (100% similar), create combined image to show differences
            combined = imageComparisonUtils.mergeImagesAndHighlightDifferences(baseImage, actualImage,
                    combined, responseObject.getDiff_coordinates());
            logger.info("Combined image created with dimensions: " +
                    combined.getWidth() + "x" + combined.getHeight());
            File combinedImage = null;
            combinedImage = File.createTempFile("combined", ".png");
            logger.info("Combined image file created at: " + combinedImage.getAbsolutePath());
            StringBuilder errorMessage = new StringBuilder("Images are identical (100% similar), " +
                    "percentage similarity is: " + (percentageSimilarity * 100) + "%. " +
                    "Check step screenshot for visual results");
            return uploadScreenshot(false, file2,
                    combined, combinedImage, errorMessage);
        } catch (IOException e) {
            logger.info("image not found ");
            logger.info(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs the API call to the visual testing server to compare two images.
     * returns percentage match of image.
     *
     * @param baseImage
     * @param actualImage
     * @return
     */
    public double performApiCall(File baseImage, File actualImage) {
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
                    logger.info(String.format("Response body for page testing: %s", responseBody));
                    responseObject = mapper.readValue(responseBody, ResponseObject.class);
                    logger.info("Deserialized the response body");
                    double percentage = responseObject.getPer_similar();
                    logger.info("Percentage similarity: " + percentage * 100);
                    return percentage;
                } else {
                    setErrorMessage("Visual testing failed no response body " +
                            "present in the visual test response");
                    throw new RuntimeException("Visual testing failed with no response body");
                }
            } else {
                setErrorMessage("Visual testing failed  error occurred internally");
                throw new RuntimeException("Visual testing failed with internal server error");
            }
        } catch (IOException e) {

            logger.info(String.format("Exception occurred while performing visual test : %s",
                    ExceptionUtils.getStackTrace(e)));
            setErrorMessage("Unable to perform visual testing");
            throw new RuntimeException("Error occurred while performing visual test ");
        }
    }

    private Result uploadScreenshot(boolean compareResult, File actualDirImage, BufferedImage combined,
                                    File combinedImage, StringBuilder errorMessageBuilder) {
        try {
            ImageComparisonUtils imageComparisonUtils = new ImageComparisonUtils(driver, logger);
            String s3Url = testStepResult.getScreenshotUrl();
            if (compareResult) {
                logger.info("images are dissimilar hence uploading the actual image to S3");
                boolean uploadS3Result = imageComparisonUtils.uploadFile(s3Url, actualDirImage.getAbsolutePath());
                if (!uploadS3Result) {
                    logger.info("Error occurred while uploading actual image to s3," +
                            " screenshot might not be displayed");
                }
                logger.info("Upload complete.");
                setSuccessMessage("Successfully verified that the base image and actual image are " +
                        "dissimilar by visual testing. (Note: Step screenshot contains the actual image)");
            } else {
                ImageIO.write(combined, "png", combinedImage);
                boolean uploadS3Result = imageComparisonUtils.uploadFile(s3Url, combinedImage.getAbsolutePath());
                if (!uploadS3Result) {
                    logger.debug("Error occurred while uploading combined image to S3," +
                            " screenshot might not be displayed");
                }
//                String message = "Comparison failed because visual testing detected identical images," +
//                        " check step screenshot for visual results";
//                errorMessageBuilder.append(message);
                setErrorMessage(errorMessageBuilder.toString());
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            String message = "Unable to perform the operation during screenshot upload: " + e.getMessage();
            errorMessageBuilder.append(message);
            setErrorMessage(message);
            return Result.FAILED;
        }
        logger.info("Successfully uploaded screenshot to S3: " + testStepResult.getScreenshotUrl());
        return Result.SUCCESS;
    }

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
                }
                File tempFile = File.createTempFile(baseName, extension);
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
//                return createLocalFileFromDownloadsCopy(url, ".png");
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given file, please check the given inputs.");
        }
    }


} 