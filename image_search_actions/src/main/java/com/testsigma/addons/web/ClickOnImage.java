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
import org.openqa.selenium.interactions.Actions;

import java.io.File;

@Data
@Action(actionText = "Click on image-url",
        description = "Click on give image",
        applicationType = ApplicationType.WEB)
public class ClickOnImage extends WebAction {
    @TestData(reference = "image-url")
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
        int x1 = response.getX1();
        int x2 = response.getX2();
        int y1 = response.getY1();
        int y2 = response.getY2();
        Double clickLocationX = (double) ((x1 + x2) / 2);
        Double clickLocationY = (double) ((y1 + y2) / 2);

        Actions actions = new Actions(driver);
        actions.moveByOffset(clickLocationX.intValue(), clickLocationY.intValue()).click().build().perform();
        setSuccessMessage("Image Found :" + response.getIsFound() +
                "    Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
        return Result.SUCCESS;
    }
}