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

import java.io.*;

@Data
@Action(actionText = "Verify if image present in current-page",
        description = "Verify if the given image is present in current page",
        applicationType = ApplicationType.WEB)
public class SearchImage extends WebAction {
    @TestData(reference = "image")
    private com.testsigma.sdk.TestData testData1;


    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        TakesScreenshot scrShot =((TakesScreenshot)this.driver);
        File baseImageFile=scrShot.getScreenshotAs(OutputType.FILE);
        String url = testStepResult.getScreenshotUrl();
        ocr.uploadFile(url, baseImageFile);
        FindImageResponse response =  ocr.findImage(testData1.getValue().toString());
        setSuccessMessage("Image Found :" + response.getIsFound() +
                "     Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());
        return Result.SUCCESS;
    }

}