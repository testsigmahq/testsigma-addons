package com.testsigma.addons.android;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

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
@Action(actionText = "Do click on element by text",
        description = "Using OCR do the element click",
        applicationType = ApplicationType.ANDROID)

public class OCRTestingdo extends AndroidAction{
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "text")
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

			logger.info("text.getValue() = " + text.getValue());


			List<OCRTextPoint> TextCompare = new ArrayList<>();
			for (OCRTextPoint SimiText : textPoints) {
				logger.info("SimiText.getValue() = " + SimiText.getText());
				if (text.getValue().toString().equals(SimiText.getText())) {
					TextCompare.add(SimiText);
				}
			}
			OCRTextPoint textPoint = TextCompare.get(0);
			logger.info("Selected text to perform click " + textPoint);
			printAllCoordinates(textPoint);

			if (textPoint == null) {
				result = Result.FAILED;
				setErrorMessage("Not found");
			} else {
				logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
						", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
				clickOnCoordinates(textPoint);
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

    public void clickOnCoordinates(OCRTextPoint textPoint) {
        int x = textPoint.getX1();
        int y = textPoint.getY1();

        logger.info("Clicking on X coordinate: " + x + "\n");
        logger.info("Clicking on Y coordinate: " + y + "\n");
        
        // Using W3C Actions API instead of deprecated TouchAction
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        ((AndroidDriver) this.driver).perform(Arrays.asList(tap));
    }
}
