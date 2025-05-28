package com.testsigma.addons.android;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRImage;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;

@Data
@Action(actionText = "Store the location(X and Y coordinates) of the given text, Text Input: exacttext , x-axis-VariableName: runtimeX and y-axis-Variable-Name: runtimeY Text Occurrence Position: index (Starts from 0)",
        description = "Using OCR do the element click",
        applicationType = ApplicationType.ANDROID)
public class OCRTestingStore extends AndroidAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "exacttext")
    private com.testsigma.sdk.TestData text;
    
    @TestData(reference = "index")
    private com.testsigma.sdk.TestData index;
    
    @TestData(reference = "runtimeX", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runX;
    
    @TestData(reference = "runtimeY", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runY;
    
    
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData1;
    
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData2;

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

            if (TextCompare.size() < 1) {
                setErrorMessage("No matching text was found for comparison.");
                return Result.FAILED;
            }

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
                    
                    store(textPoint);
                
                }
            }
        }catch(Exception e) { 
            setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }


    private void store(OCRTextPoint textPoint) {
    	
    	int x = textPoint.getX1();
        int y = textPoint.getY1();
         int centerX = (textPoint.getX2() + textPoint.getX1()) / 2;
         int centerY = (textPoint.getY2() + textPoint.getY1()) / 2;

        logger.info("Store the X coordinate: " + x + "\n");
        logger.info("Store the Y coordinate: " + y + "\n");
        
        runTimeData1.setKey(runX.getValue().toString());
		runTimeData1.setValue(Integer.toString(centerX));
		
		runTimeData2.setKey(runY.getValue().toString());
		runTimeData2.setValue(Integer.toString(centerY));
		
	}


	private void printAllCoordinates(OCRTextPoint textPoint) {
      
            logger.info("text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() + ", y1 = " + textPoint.getY1() +
                    ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2() + "\n\n\n\n");
        
    }
}
