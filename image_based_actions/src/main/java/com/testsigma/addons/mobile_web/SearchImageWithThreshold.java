package com.testsigma.addons.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if image image-url is present in current-page with search threshold threshold-value (Ex: 0.9 , means 90% match)",
        description = "Verify if the give image with threshold is present in current page",
        applicationType = ApplicationType.MOBILE_WEB)
public class SearchImageWithThreshold extends com.testsigma.addons.web.SearchImageWithThreshold {
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
        super.setTestData1(testData1);
        super.setTestData2(testData2);
        super.setOcr(ocr);
        super.setTestStepResult(testStepResult);
        return super.execute();
    }
}