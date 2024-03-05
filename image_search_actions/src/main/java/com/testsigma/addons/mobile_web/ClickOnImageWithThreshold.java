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
import org.openqa.selenium.interactions.Actions;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;

import java.io.File;

@Data
@Action(actionText = "Click on image with threshold",
        description = "Click on given image with threshold",
        applicationType = ApplicationType.MOBILE_WEB)
public class ClickOnImageWithThreshold extends com.testsigma.addons.web.ClickOnImageWithThreshold {
    @TestData(reference = "image")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "threshold")
    private com.testsigma.sdk.TestData testData2;


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
        FindImageResponse response =  ocr.findImage(testData1.getValue().toString(), Float.valueOf(testData2.getValue().toString()));
        int x1 = response.getX1();
        int x2 = response.getX2();
        int y1 = response.getY1();
        int y2 = response.getY2();
        Double clickLocationX = (double) ((x1 + x2) / 2);
        Double clickLocationY = (double) ((y1 + y2) / 2);

        final TouchAction action = new TouchAction((PerformsTouchActions) driver);
        action.tap(PointOption.point(clickLocationX.intValue(), clickLocationY.intValue())).perform();
        setSuccessMessage("Image Found :" + response.getIsFound() +
                "    Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
        return Result.SUCCESS;
    }
}