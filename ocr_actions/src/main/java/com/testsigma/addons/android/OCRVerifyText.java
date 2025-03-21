package com.testsigma.addons.android;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRImage;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;

@Data
@Action(actionText = "Verify the text testdata is present on current screen",
        description = "Verify the text testdata in present on current screen",
        applicationType = ApplicationType.ANDROID)
public class OCRVerifyText extends AndroidAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData text;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            logger.info("Taking screenshot");
            File screenshot = ((TakesScreenshot)(AndroidDriver)this.driver).getScreenshotAs(OutputType.FILE);
            logger.info("Taking screenshot completed");
            OCRImage imageObj = new OCRImage();
            imageObj.setOcrImageFile(screenshot);
            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
            logger.info("Extracted text from image:" + textPoints.toString());

            List<OCRTextPoint> TextCompare = new ArrayList<>();
            for (OCRTextPoint SimiText : textPoints) {
                if (text.getValue().toString().equals(SimiText.getText())) {
                    TextCompare.add(SimiText);
                }
            }
            logger.info("Number of text available " + String.valueOf(TextCompare.size()));
            logger.info("Available text " + TextCompare);

                OCRTextPoint textPoint = TextCompare.get(0);
                logger.info("Verify text from the screen" + textPoint);
                printAllCoordinates(textPoint);

                if (textPoint == null) {
                    result = Result.FAILED;
                    setErrorMessage("Not found");
                } else {
                    logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                            ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                    setSuccessMessage("The text was present on the screen : " +textPoint);
                }
        }catch(Exception e) { 
            setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }


    private void printAllCoordinates(OCRTextPoint textPoint) {
      
            logger.info("text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() + ", y1 = " + textPoint.getY1() +
                    ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2() + "\n\n\n\n");
        
    }
}
