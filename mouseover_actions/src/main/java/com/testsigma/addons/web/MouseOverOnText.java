package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRImage;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.util.List;

@Data
@Action(actionText = "Perform mouse over on text test-data",
        description = "Takes a screenshot, extracts text coordinates using OCR, and performs a mouse over on the first occurrence of the specified text.",
        applicationType = ApplicationType.WEB)
public class MouseOverOnText extends WebAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @Override
    protected Result execute() throws NoSuchElementException {
        try {
            String targetText = testData.getValue().toString();
            logger.info("Performing mouse over on text: " + targetText);

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            OCRImage ocrImage = new OCRImage();
            ocrImage.setOcrImageFile(screenshot);
            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
            logger.info("Extracted " + textPoints.size() + " text points from screenshot");

            OCRTextPoint matchedPoint = findTextPoint(textPoints, targetText);
            if (matchedPoint == null) {
                logAvailableTextPoints(textPoints);
                setErrorMessage("Text '" + targetText + "' was not found on the screen.");
                return Result.FAILED;
            }

            int x = (matchedPoint.getX1() + matchedPoint.getX2()) / 2;
            int y = (matchedPoint.getY1() + matchedPoint.getY2()) / 2;
            logger.info("Found text '" + matchedPoint.getText() + "' at center (" + x + ", " + y + ")");

            Actions actions = new Actions(driver);
            actions.moveToLocation(x, y).perform();

            setSuccessMessage("Successfully performed mouse over on text '" + targetText + "' at (" + x + ", " + y + ")");
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Error performing mouse over on text: " + e.getMessage());
            setErrorMessage("Failed to perform mouse over on text: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    private OCRTextPoint findTextPoint(List<OCRTextPoint> textPoints, String targetText) {
        if (textPoints == null) return null;
        for (OCRTextPoint tp : textPoints) {
            if (targetText.equals(tp.getText())) return tp;
        }
        for (OCRTextPoint tp : textPoints) {
            if (targetText.equalsIgnoreCase(tp.getText())) return tp;
        }
        for (OCRTextPoint tp : textPoints) {
            if (tp.getText().toLowerCase().contains(targetText.toLowerCase())) return tp;
        }
        return null;
    }

    private void logAvailableTextPoints(List<OCRTextPoint> textPoints) {
        logger.info("Available text elements on screen:");
        for (OCRTextPoint tp : textPoints) {
            logger.info("  '" + tp.getText() + "' at x1=" + tp.getX1() + ",y1=" + tp.getY1()
                    + ",x2=" + tp.getX2() + ",y2=" + tp.getY2());
        }
    }
}
