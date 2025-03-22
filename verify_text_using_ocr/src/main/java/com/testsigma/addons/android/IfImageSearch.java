package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

@Data
@Action(actionText = "Verify if image image-file is present in the current page",
        description = "Verifies if the given image is present in the current page using OCR (If Condition)",
        applicationType = ApplicationType.ANDROID,
        actionType = StepActionType.IF_CONDITION,
        useCustomScreenshot = true)
public class IfImageSearch extends AndroidAction {

    @TestData(reference = "image-file")
    private com.testsigma.sdk.TestData testData1;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        Result result = Result.FAILED;

        try {
            AndroidDriver androidDriver = (AndroidDriver) this.driver;

            // Validate input test data
            if (testData1 == null || testData1.getValue() == null || testData1.getValue().toString().isEmpty()) {
                setErrorMessage("Image file path is not provided or invalid.");
                logger.info("Test data 'image-file' is null or empty.");
                return result;
            }

            // Take a screenshot of the current page
            File baseImageFile = ((TakesScreenshot) androidDriver).getScreenshotAs(OutputType.FILE);

            // Upload the screenshot to OCR service
            String url = testStepResult.getScreenshotUrl();
            logger.info("Uploading base image to OCR service. Amazon S3 URL: " + url);
            ocr.uploadFile(url, baseImageFile);

            // Search for the specified image
            logger.info("Searching for image: " + testData1.getValue());
            FindImageResponse response = ocr.findImage(testData1.getValue().toString());

            // Evaluate the response
            if (response.getIsFound()) {
                setSuccessMessage("Image Found: " + response.getIsFound() +
                        " | Coordinates: x1=" + response.getX1() + ", x2=" + response.getX2() +
                        ", y1=" + response.getY1() + ", y2=" + response.getY2());
                logger.info("Image found successfully.");
                result = Result.SUCCESS;
            } else {
                setErrorMessage("Image not found on the current page.");
                logger.warn("Image not found for the given file: " + testData1.getValue());
            }

        } catch (Exception e) {
            // Handle any unexpected exceptions
            setErrorMessage("Exception occurred while searching for the image: " + e.getMessage());
            logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
        }

        return result;
    }
}