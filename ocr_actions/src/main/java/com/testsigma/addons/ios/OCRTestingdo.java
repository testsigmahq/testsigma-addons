package com.testsigma.addons.ios;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Actions;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.OCRImage;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;

@Data
@Action(actionText = "Do click on element by text",
description = "Using OCR do the element click",
applicationType = ApplicationType.IOS)

public class OCRTestingdo extends IOSAction{
	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "text")
	private com.testsigma.sdk.TestData text;

	@Override
	protected Result execute() {
		Result result = Result.SUCCESS;
		try {
			logger.info("Taking screenshot");
			File screenshot = ((TakesScreenshot)(IOSDriver)this.driver).getScreenshotAs(OutputType.FILE);
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
		
		TouchAction action = new TouchAction((IOSDriver)this.driver);
	    action.tap(PointOption.point(x,y)).perform();

		//TouchAction action = new TouchAction((PerformsTouchActions) (IOSDriver)this.driver);
		//action.tap(PointOption.point(x, y)).perform();
	}
}
