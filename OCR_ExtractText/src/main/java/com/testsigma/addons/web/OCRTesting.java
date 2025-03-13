package com.testsigma.addons.web;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Actions;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRImage;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;

@Data
@Action(actionText = "Extract text from the locator element and store into a variable testdata",
description = "Using OCR extract the text from the element",
applicationType = ApplicationType.WEB)
public class OCRTesting extends WebAction {
	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "testdata", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData text;

	@Element(reference = "element")
	private com.testsigma.sdk.Element element;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	protected Result execute() {
		Result result = Result.SUCCESS;
		StringBuilder sb = new StringBuilder();
		try {
			logger.info("Taking screenshot");
			File screenshot = element.getElement().getScreenshotAs(OutputType.FILE);
			logger.info("Taking screenshot completed,Local File Path:"+screenshot.getAbsolutePath());
			OCRImage imageObj = new OCRImage();
			imageObj.setOcrImageFile(screenshot);
			List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
			logger.info("Extracted text from image:" + textPoints.toString());
            String extracted = textPoints.get(textPoints.size()-1).getText();
            String formattedString = extractTextAndNumber(extracted);

			for (OCRTextPoint SimiText : textPoints) { 
						logger.info("Value::"+SimiText.getText()+"<br>");
					}

			runTimeData.setKey(text.getValue().toString());
			runTimeData.setValue(formattedString);

			setSuccessMessage("The Text was extracted and stored into a variable"+ " : " +text.getValue().toString() + ": and the value is:" +formattedString);
			logger.info("The Text was extracted and stored into a variable" + " : " +text.getValue().toString() + ": and the value is:" +formattedString);

		}catch(Exception e) { 
			setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
			result = Result.FAILED;
		}
		return result;
	}
	  public static String extractTextAndNumber(String input) {
	        StringBuilder result = new StringBuilder();

	        for (char ch : input.toCharArray()) {
	            if (Character.isDigit(ch)) {
	                result.append(ch);
	            } else if (Character.isLetter(ch)) {
	                result.append(ch);
	            }
	        }

	        return result.toString();
	    }
}
