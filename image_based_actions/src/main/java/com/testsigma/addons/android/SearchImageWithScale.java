package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

@Data
@Action(actionText = "Verify if image image-url is present in current-page with search threshold threshold-value (Ex: 0.9 , means 90% match) and scale as scale-factor",
        description = "Verify if the given image with threshold and scale is present in the current page",
        applicationType = ApplicationType.ANDROID)
public class SearchImageWithScale extends AndroidAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "scale-factor")
    private com.testsigma.sdk.TestData testData3;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        Result result = Result.SUCCESS;
        File baseImageFile= ((TakesScreenshot)androidDriver).getScreenshotAs(OutputType.FILE);
        String url = testStepResult.getScreenshotUrl();
        logger.info("Amazon s3 url in which we are storing base image"+url);
        ocr.uploadFile(url, baseImageFile);
        FindImageResponse response =  ocr.findImage(testData1.getValue().toString(), Float.valueOf(testData2.getValue().toString()), Integer.parseInt(testData3.getValue().toString()));
        if(response.getIsFound()){
            setSuccessMessage("Image Found :" + response.getIsFound() +
                    "     Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());

        } else {
            setErrorMessage("Given image not found in the current page");
            result = Result.FAILED;
        }
        return result;

    }
}