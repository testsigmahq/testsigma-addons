package com.testsigma.addons.web;

import java.util.List;
import java.io.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

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
@Action(actionText = "Extract text from the position position_data of text text_data and store into a variable testdata",
description = "Using OCR extract the text from the screen",
applicationType = ApplicationType.WEB)
public class OCRextract extends WebAction {
	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "text_data")
	private com.testsigma.sdk.TestData testdata1;

	@TestData(reference = "position_data")
	private com.testsigma.sdk.TestData testdata2;
	
	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testdata3;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	protected Result execute() {
		Result result = Result.SUCCESS;
		try {
			logger.info("Taking screenshot");
            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            logger.info("Taking screenshot completed");
            OCRImage imageObj = new OCRImage();
            imageObj.setOcrImageFile(screenshot);
			List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
			logger.info("Extracted text from image:" + textPoints.toString());
			String mainstring = testdata1.getValue().toString();
	        int position = Integer.parseInt(testdata2.getValue().toString());
            String text = textPoints.get(textPoints.size()-1).getText();
            
            int index = -1;
            for (int i = 0; i < textPoints.size(); i++) {
                if (textPoints.get(i).getText().equals(mainstring)) {
                    index = i;
                    break;
                }
            }
            if (index != -1 && index + position < textPoints.size()) {
                OCRTextPoint nextPoint = textPoints.get(index + position);
                String extracted = nextPoint.getText();
                runTimeData.setKey(testdata3.getValue().toString());
    			runTimeData.setValue(extracted);

    			setSuccessMessage("The Text was extracted and stored into a variable"+ " : " +testdata3.getValue().toString() + ": and the value is:" +extracted);
    			logger.info("The Text was extracted and stored into a variable"+ " : " +testdata3.getValue().toString() + ": and the value is:" +extracted);
            } else {
            	setErrorMessage("Substring not found in the text. There are not enought words present" +mainstring);
            	logger.warn("Substring not found in the text. There are not enought words present" +mainstring);
    			result = Result.FAILED;
            }
		}catch(Exception e) { 
			setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
			result = Result.FAILED;
		}
		return result;
	}
}
