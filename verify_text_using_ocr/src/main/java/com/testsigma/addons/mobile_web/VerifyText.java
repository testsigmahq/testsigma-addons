package com.testsigma.addons.mobile_web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This class defines a custom WebAction for verifying if a specific text is present on the screen
 * by using Optical Character Recognition (OCR) to extract text from a screenshot.
 */
@Data
@Action(
    actionText = "Verify the text testdata present on the screen using OCR",
    description = "Using OCR to verify that the specified text is present on the screen",
    applicationType = ApplicationType.MOBILE_WEB,
    useCustomScreenshot = false
)
public class VerifyText extends WebAction {

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testdata;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Log the test data value
            String textToVerify = testdata.getValue().toString();
            logger.info("Text to verify: " + textToVerify);

            AppiumDriver appiumDriver = (AppiumDriver) driver;

            // Capture a screenshot of the current view
            File screenshot = ((TakesScreenshot) appiumDriver).getScreenshotAs(OutputType.FILE);
            logger.info("Screenshot taken");

            // Create OCRImage object from the screenshot
            OCRImage imageObj = new OCRImage();
            imageObj.setOcrImageFile(screenshot);

            // Extract text points from the image
            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
            logger.info("Extracted text from image: " + textPoints);

            // Check if the extracted text contains the text to verify
            Optional<OCRTextPoint> matchedTextPoint = textPoints.stream()
                .filter(textPoint -> textPoint.getText().contains(textToVerify))
                .findFirst();

            if (matchedTextPoint.isPresent()) {
                // Text was found in the image
                setSuccessMessage("Text \"" + textToVerify + "\" found in the current screen.");
            } else {
                // Text was not found in the image
                result = Result.FAILED;
                setErrorMessage("Text \"" + textToVerify + "\" not found in the current screen.");
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during execution
            setErrorMessage("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
}
