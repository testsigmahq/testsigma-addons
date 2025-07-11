package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

@Data
@Action(actionText = "Verify if image image-url is not present in current-page with search threshold threshold-value (Ex: 0.9 , means 90% match)",
        description = "Verify if the given image with threshold and scale is not present in the current page",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)
public class VerifyImageNotPresentOnScreenWithThreshold extends WebAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageURL;
    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData thresholdValue;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {

        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        try {
            String imageURL_ = imageURL.getValue().toString();
            logger.info("Taking screenshot");
            File baseImageFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            logger.info("Screenshot taken :" + baseImageFile.getAbsolutePath());
            logger.info("Taking screenshot completed");
            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon s3 url in which we are storing base image"+url);
            ocr.uploadFile(url, baseImageFile);
            FindImageResponse response = ocr.findImage(imageURL_,Float.valueOf(thresholdValue.getValue().toString()));
            logger.info("response :" + response);
            if (response == null) {
                logger.info("Given image not found in the current page");
                setSuccessMessage("Given image not found in the current page");
            } else {
                logger.warn("Image Found :" + response.getIsFound() +
                        "     Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());
                setErrorMessage("Image Found :" + response.getIsFound() +
                        "     Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());

                result = Result.FAILED;
            }

        } catch (Exception e) {
            result = Result.FAILED;
            logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception: " + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}
