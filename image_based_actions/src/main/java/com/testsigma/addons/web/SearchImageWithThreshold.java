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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

@Data
@Action(actionText = "Verify if image image-url is present in current-page with search threshold threshold-value (Ex: 0.9 , means 90% match)",
        description = "Verify if the give image with threshold is present in current page",
        applicationType = ApplicationType.WEB)
public class SearchImageWithThreshold extends WebAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData testData2;


    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        TakesScreenshot scrShot =((TakesScreenshot)this.driver);
        File baseImageFile=scrShot.getScreenshotAs(OutputType.FILE);
        String url = testStepResult.getScreenshotUrl();
        logger.info("Amazon s3 url in which we are storing base image"+url);
        ocr.uploadFile(url, baseImageFile);
        FindImageResponse response =  ocr.findImage(testData1.getValue().toString(), Float.valueOf(testData2.getValue().toString()));
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