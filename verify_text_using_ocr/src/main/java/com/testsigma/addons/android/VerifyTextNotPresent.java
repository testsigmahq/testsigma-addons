package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This class defines an AndroidAction for verifying if a specific text is present on the screen
 * by using Optical Character Recognition (OCR) to extract text from a screenshot.
 */
@Data
@Action(
    actionText = "Verify the text testdata NOT present on the screen using OCR",
    description = "Using OCR to verify that the specified text is present on the screen",
    applicationType = ApplicationType.ANDROID
)
public class VerifyTextNotPresent extends AndroidAction {

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

            // Capture a screenshot of the current view
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            File screenshotFile = ((TakesScreenshot) androidDriver).getScreenshotAs(OutputType.FILE);
            //BufferedImage bufferedImage = ImageIO.read(screenshotFile);

            // Create OCRImage object from the screenshot
            com.testsigma.sdk.OCRImage imageObj = new com.testsigma.sdk.OCRImage();
            imageObj.setOcrImageFile(screenshotFile);

            // Extract text points from the image
            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
            logger.info("Extracted text from image: " + textPoints);

            // Check if the extracted text contains the text to verify
            Optional<OCRTextPoint> matchedTextPoint = textPoints.stream()
                .filter(textPoint -> textPoint.getText().contains(textToVerify))
                .findFirst();

            if (matchedTextPoint.isPresent()==false) {
                // Text was found in the image
                setSuccessMessage("Text \"" + textToVerify + "\" not found on the screen.");
            } else {
                // Text was not found in the image
                result = Result.FAILED;
                setErrorMessage("Text \"" + textToVerify + "\" found on the screen.");
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during execution
            setErrorMessage("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
}
