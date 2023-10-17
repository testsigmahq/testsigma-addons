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

import java.io.*;

@Data
@Action(actionText = "Verify if image present in current-page",
        description = "Verify if the given image is present in current page",
        applicationType = ApplicationType.MOBILE_WEB)
public class SearchImage extends com.testsigma.addons.web.SearchImage {
    @TestData(reference = "image")
    private com.testsigma.sdk.TestData testData1;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        super.setTestData1(testData1);
        super.setOcr(ocr);
        super.setTestStepResult(testStepResult);
        return super.execute();
    }

}