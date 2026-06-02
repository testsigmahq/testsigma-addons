package com.testsigma.addons.mobileweb;

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
@Action(actionText = "Perform mouse over on text target-text at occurrence occurrence-position",
        description = "Takes a screenshot, extracts text coordinates using OCR, and performs a mouse over on the Nth occurrence (1-based) of the specified text.",
        applicationType = ApplicationType.MOBILE_WEB)
public class MouseOverOnTextAtOccurrence extends WebAction {

    @TestData(reference = "target-text")
    private com.testsigma.sdk.TestData targetText;

    @TestData(reference = "occurrence-position")
    private com.testsigma.sdk.TestData occurrencePosition;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @Override
    protected Result execute() throws NoSuchElementException {
        try {
            String text = targetText.getValue().toString();
            int occurrence = Integer.parseInt(occurrencePosition.getValue().toString());

            if (occurrence < 1) {
                setErrorMessage("Occurrence position must be >= 1 (1-based indexing). Received: " + occurrence);
                return Result.FAILED;
            }

            logger.info("Performing mouse over on text: '" + text + "' at occurrence: " + occurrence);

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            OCRImage ocrImage = new OCRImage();
            ocrImage.setOcrImageFile(screenshot);
            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
            logger.info("Extracted " + textPoints.size() + " text points from screenshot");

            OCRTextPoint matchedPoint = findTextPointAtOccurrence(textPoints, text, occurrence);
            if (matchedPoint == null) {
                int totalFound = countMatches(textPoints, text);
                logAvailableTextPoints(textPoints);
                setErrorMessage("Text '" + text + "' occurrence " + occurrence + " not found. "
                        + "Total occurrences found: " + totalFound);
                return Result.FAILED;
            }

            int x = (matchedPoint.getX1() + matchedPoint.getX2()) / 2;
            int y = (matchedPoint.getY1() + matchedPoint.getY2()) / 2;
            logger.info("Found text '" + matchedPoint.getText() + "' (occurrence " + occurrence + ") at center (" + x + ", " + y + ")");

            Actions actions = new Actions(driver);
            actions.moveToLocation(x, y).perform();

            setSuccessMessage("Successfully performed mouse over on text '" + text
                    + "' (occurrence " + occurrence + ") at (" + x + ", " + y + ")");
            return Result.SUCCESS;
        } catch (NumberFormatException e) {
            setErrorMessage("Invalid occurrence position. Must be a number: " + occurrencePosition.getValue());
            return Result.FAILED;
        } catch (Exception e) {
            logger.warn("Error performing mouse over on text: " + e.getMessage());
            setErrorMessage("Failed to perform mouse over on text: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    private OCRTextPoint findTextPointAtOccurrence(List<OCRTextPoint> textPoints, String text, int targetOccurrence) {
        if (textPoints == null) return null;
        int currentOccurrence = 0;
        for (OCRTextPoint tp : textPoints) {
            if (text.equals(tp.getText()) || text.equalsIgnoreCase(tp.getText())) {
                currentOccurrence++;
                if (currentOccurrence == targetOccurrence) return tp;
            }
        }
        return null;
    }

    private int countMatches(List<OCRTextPoint> textPoints, String text) {
        if (textPoints == null) return 0;
        int count = 0;
        for (OCRTextPoint tp : textPoints) {
            if (text.equalsIgnoreCase(tp.getText())) count++;
        }
        return count;
    }

    private void logAvailableTextPoints(List<OCRTextPoint> textPoints) {
        logger.info("Available text elements on screen:");
        for (OCRTextPoint tp : textPoints) {
            logger.info("  '" + tp.getText() + "' at x1=" + tp.getX1() + ",y1=" + tp.getY1()
                    + ",x2=" + tp.getX2() + ",y2=" + tp.getY2());
        }
    }
}
