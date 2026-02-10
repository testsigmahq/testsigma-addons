package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import java.io.File;
import java.util.List;

@Data
@Action(actionText = "Verify the text testdata present in the screen",
        description = "Using OCR verifying that the text present in the screen",
        applicationType = ApplicationType.WEB,
		actionType = StepActionType.WHILE_LOOP,
        useCustomScreenshot = false)
public class VerifyTextWeb_TSWhileLoop extends WebAction {

	 @OCR
	    private com.testsigma.sdk.OCR ocr;

	    @TestData(reference = "testdata")
	    private com.testsigma.sdk.TestData testdata;
	    @Override
	    protected Result execute() {
	        Result result = Result.SUCCESS;
	        try {
	        	logger.info("Text = " + testdata.getValue().toString());
	            logger.info("Taking screenshot");
	            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
	            logger.info("Taking screenshot completed");
	            OCRImage imageObj = new OCRImage();
	            imageObj.setOcrImageFile(screenshot);
	            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
	            logger.info("Extracted text from image:"+textPoints.toString());
	            
	            OCRTextPoint TextCompare = null;

	            for (OCRTextPoint SimiText : textPoints) 
	            {
					if (SimiText.getText().contains(testdata.getValue().toString())) 
					{
						TextCompare = SimiText;
					} 
				}
	            
	            if (TextCompare == null) {
	                result = Result.FAILED;
	                setErrorMessage("Text " + testdata.getValue().toString() + " not found in the current screen");
	            }
	            else 
	            {
	            	setSuccessMessage("Text " + testdata.getValue().toString() +" found in the current screen");
	            }
	        }catch(Exception e) { 
	            setErrorMessage(ExceptionUtils.getStackTrace(e));
	            result = Result.FAILED;
	        }
	        return result;
	    }
}