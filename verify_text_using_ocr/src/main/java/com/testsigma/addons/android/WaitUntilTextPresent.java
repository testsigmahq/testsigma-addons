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
 * This class defines an AndroidAction that waits until a specific text is
 * present on the screen by using Optical Character Recognition (OCR) to extract
 * text from repeated screenshots.
 */
@Data
@Action(actionText = "Wait until the text testdata1 is present on the screen for testdata2 seconds using OCR", description = "Using OCR to wait until the specified text appears on the screen", applicationType = ApplicationType.ANDROID)
public class WaitUntilTextPresent extends AndroidAction {

	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "testdata1")
	private com.testsigma.sdk.TestData textValue;

	@TestData(reference = "testdata2")
	private com.testsigma.sdk.TestData maxWaitTimeValue; // Maximum wait time in seconds

	@Override
	protected Result execute() {
		Result result = Result.FAILED;
		try {
			String textToWaitFor = textValue.getValue().toString();
			int maxWaitTime = Integer.parseInt(maxWaitTimeValue.getValue().toString()); // Max wait time in seconds
			int interval = maxWaitTime / 10; // Polling interval in seconds
			int elapsedTime = 0;

			logger.info("Waiting for text: \"" + textToWaitFor + "\" with a maximum wait time of " + maxWaitTime
					+ " seconds.");

			AndroidDriver androidDriver = (AndroidDriver) this.driver;

			// Loop until the text is found or the timeout is reached
			while (elapsedTime < maxWaitTime) {
				// Capture a screenshot of the current view
				File screenshotFile = ((TakesScreenshot) androidDriver).getScreenshotAs(OutputType.FILE);

				// Create OCRImage object from the screenshot
				com.testsigma.sdk.OCRImage imageObj = new com.testsigma.sdk.OCRImage();
				imageObj.setOcrImageFile(screenshotFile);

				// Extract text points from the image
				List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
				logger.info("Checking extracted text at elapsed time: " + elapsedTime + " seconds.");
				logger.info("Extracted text from image: " + textPoints);

				// Check if any of the extracted text matches the specified text
				Optional<OCRTextPoint> matchedTextPoint = textPoints.stream()
						.filter(textPoint -> textPoint.getText().contains(textToWaitFor)).findFirst();

				if (matchedTextPoint.isPresent()) {
					// Text was found in the image, set success
					setSuccessMessage("Text \"" + textToWaitFor + "\" found on the screen.");
					logger.info("Text \"" + textToWaitFor + "\" found after " + elapsedTime);
					result = Result.SUCCESS;
					break;
				}

				// Wait for the interval and increment elapsed time
				Thread.sleep(interval * 1000);
				elapsedTime += interval;

				// Logging to indicate ongoing wait
				logger.info("Text \"" + textToWaitFor + "\" not yet found. Continuing to wait...");
			}

			if (result == Result.FAILED) {
				// If loop completes without finding the text, set failure message
				setErrorMessage(
						"Text \"" + textToWaitFor + "\" not found on the screen within " + maxWaitTime + " seconds.");
			}
		} catch (Exception e) {
			// Handle any exceptions that occur during execution
			setErrorMessage("Error occurred: " + ExceptionUtils.getStackTrace(e));
			result = Result.FAILED;
		}
		return result;
	}
}