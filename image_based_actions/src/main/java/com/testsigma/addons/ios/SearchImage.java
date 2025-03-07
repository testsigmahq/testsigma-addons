package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

@Data
@Action(actionText = "Verify if image image-file present in current-page",
        description = "Verify if the given image is present in current page",
        applicationType = ApplicationType.IOS)
public class SearchImage extends IOSAction {
    @TestData(reference = "image-file")
    private com.testsigma.sdk.TestData testData1;


    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        IOSDriver iosDriver = (IOSDriver) this.driver;
        Result result = Result.SUCCESS;
        File baseImageFile= ((TakesScreenshot)iosDriver).getScreenshotAs(OutputType.FILE);
        String url = testStepResult.getScreenshotUrl();
        logger.info("Amazon s3 url in which we are storing base image"+url);
        ocr.uploadFile(url, baseImageFile);
        FindImageResponse response =  ocr.findImage(testData1.getValue().toString());
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