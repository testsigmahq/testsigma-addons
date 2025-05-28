package com.testsigma.addons.windows;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Store the location(X and Y coordinates) of the given text, Text Input: exacttext , x-axis-VariableName: runtimeX and y-axis-Variable-Name: runtimeY Text Occurrence Position: index (Starts from 0)",
        description = "Using OCR do the element click",
        applicationType = ApplicationType.WINDOWS)
public class OCRTestingStore extends WindowsAction {
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
            logger.info("Starting OCR text location storage action.");
            logger.info("Taking screenshot");
            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
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
            logger.info("Selected Index to find the coordinates from :" + selectedIndex);

            if (selectedIndex < 0 || selectedIndex >= TextCompare.size()) {
                logger.warn("Invalid index provided: " + selectedIndex + ". Index should be between 0 and " + (TextCompare.size() - 1));
                result = Result.FAILED;
                setErrorMessage("Invalid index. Index should be between 0 and " + (TextCompare.size() - 1));
                return result;
            } else {
                OCRTextPoint textPoint = TextCompare.get(selectedIndex);
                logger.info("Selected text to perform click " + textPoint);
                printAllCoordinates(textPoint);

                if (textPoint == null) {
                    logger.warn("Textpoint is null, cannot find coordinates. Check if the element is present in the DOM or if the Index value is correct.");
                    result = Result.FAILED;
                    setErrorMessage("Not found. Check logs for more details.");
                    return result;
                } else {
                    logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                            ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());

                    store(textPoint);
                    logger.info("X and Y coordinates stored successfully to Runtime variables.");

                }
            }
        }catch(Exception e) {
            logger.warn("Error occurred while finding/storing the coordinates: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            return result;
        }
        return result;
    }


    private void store(OCRTextPoint textPoint) {

        int x = textPoint.getX1();
        int y = textPoint.getY1();
        int centerX = (textPoint.getX2() + textPoint.getX1()) / 2;
        int centerY = (textPoint.getY2() + textPoint.getY1()) / 2;

        logger.info("X coordinate: " + x);
        logger.info("Y coordinate: " + y);
        logger.info("Center X coordinate: " + centerX);
        logger.info("Center Y coordinate: " + centerY);

        runTimeData1.setKey(runX.getValue().toString());
        runTimeData1.setValue(Integer.toString(centerX));

        runTimeData2.setKey(runY.getValue().toString());
        runTimeData2.setValue(Integer.toString(centerY));

        setSuccessMessage(String.format("Successfully stored X: %s and Y: %s coordinates to runtime variables %s and %s", centerX, centerY, runX.getValue(), runY.getValue()));

    }


    private void printAllCoordinates(OCRTextPoint textPoint) {
        logger.info("Text Details: ");
        logger.info("text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() + ", y1 = " + textPoint.getY1() +
                ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2() + "\n\n\n\n");

    }
}
