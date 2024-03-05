package com.testsigma.addons.mobile_web;

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
@Action(actionText = "Verify if image present in current-page with threshold and scale",
        description = "Verfy if the given image with threshold and scale is present in the current page",
        applicationType = ApplicationType.MOBILE_WEB)
public class SearchImageWithScale extends com.testsigma.addons.web.SearchImageWithScale {
    @TestData(reference = "image")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "threshold")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "scale")
    private com.testsigma.sdk.TestData testData3;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        super.setTestData1(testData1);
        super.setTestData2(testData2);
        super.setTestData3(testData3);
        super.setOcr(ocr);
        super.setTestStepResult(testStepResult);
        return super.execute();
    }
}