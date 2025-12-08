package com.testsigma.addons.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.time.Duration;

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
@Action(actionText = "Move the curser and click on the element by exacttext with index",
description = "Using OCR do the element click",
applicationType = ApplicationType.ANDROID)
public class OCRTestingmouse extends AndroidAction {
	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "exacttext")
	private com.testsigma.sdk.TestData text;

	@TestData(reference = "index")
	private com.testsigma.sdk.TestData index;

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

			int selectedIndex = Integer.parseInt(index.getValue().toString());

			if (selectedIndex < 0 || selectedIndex >= TextCompare.size()) {
				logger.info("Invalid index provided: " + selectedIndex);
				result = Result.FAILED;
				setErrorMessage("Invalid index");
			} else {
				OCRTextPoint textPoint = TextCompare.get(selectedIndex);
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

	public void clickOnCoordinates(OCRTextPoint textPoint) throws InterruptedException {

		int x = textPoint.getX1();
		int y = textPoint.getY1();

		logger.info("Clicking on X coordinate: " + x + "\n");
		logger.info("Clicking on Y coordinate: " + y + "\n");
		
		// Using W3C Actions API instead of deprecated TouchAction
		PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
		Sequence tap = new Sequence(finger, 1);
		logger.info("Pressing X coordinate: " + x + "\n");
		logger.info("Pressing Y coordinate: " + y + "\n");
		// Perform the move and tap action
		tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
		tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
		tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
		((AndroidDriver) this.driver).perform(Arrays.asList(tap));
		logger.info("Successfully pressed X and y coordinate: " + x + "\n" + y + "\n");
	}
}
