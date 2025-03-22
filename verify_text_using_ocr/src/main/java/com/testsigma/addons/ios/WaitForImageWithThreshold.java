package com.testsigma.addons.ios;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;

@Data
@Action(actionText = "Wait until image image-upload-path is present with applied search threshold threshold-value (e.g., 0.9 means 90% match), max wait time max-wait-time seconds", description = "Waits until the specified image is detected on screen using a given threshold within a maximum wait time", applicationType = ApplicationType.IOS)
public class WaitForImageWithThreshold extends IOSAction {

	@TestData(reference = "image-upload-path")
	private com.testsigma.sdk.TestData imageUploadPath;

	@TestData(reference = "threshold-value")
	private com.testsigma.sdk.TestData thresholdValue;

	@TestData(reference = "max-wait-time")
	private com.testsigma.sdk.TestData maxWaitTimeValue; // Maximum wait time in seconds

	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestStepResult
	private com.testsigma.sdk.TestStepResult testStepResult;

	@Override
	protected Result execute() throws NoSuchElementException {
		Result result = Result.FAILED;
		try {
			IOSDriver iosDriver = (IOSDriver) this.driver;
			File screenshotFile;
			int maxWaitTime = Integer.parseInt(maxWaitTimeValue.getValue().toString()); // Max wait time in seconds
			int interval = Math.max(maxWaitTime / 10, 1); // Ensure a minimum interval of 1 second
			int elapsedTime = 0;

			float threshold = Float.parseFloat(thresholdValue.getValue().toString());
			if (threshold < 0 || threshold > 1) {
				throw new IllegalArgumentException("Threshold value must be between 0 and 1");
			}

			logger.info("Waiting for image to appear within " + maxWaitTime + " seconds.");

			while (elapsedTime < maxWaitTime) {
				// Capture current screen as a file
				screenshotFile = ((TakesScreenshot) iosDriver).getScreenshotAs(OutputType.FILE);

				try {
					String s3Url = testStepResult.getScreenshotUrl();
					ocr.uploadFile(s3Url, screenshotFile);

					logger.info("Searching for image: " + imageUploadPath.getValue());

					FindImageResponse response = ocr.findImage(imageUploadPath.getValue().toString(), threshold);

					if (response.getIsFound()) {
						logger.info("Image found at coordinates: x1=" + response.getX1() + ", x2=" + response.getX2()
								+ ", y1=" + response.getY1() + ", y2=" + response.getY2());
						setSuccessMessage("Image found after " + elapsedTime + " seconds.");
						result = Result.SUCCESS;
						break;
					}
				} catch (Exception e) {
					logger.warn("OCR operation failed: " + e.getMessage());
				}

				// Wait before the next check
				Thread.sleep(interval * 1000);
				elapsedTime += interval;
			}

			if (result == Result.FAILED) {
				setErrorMessage("Image not found on the screen within the specified maximum wait time of " + maxWaitTime
						+ " seconds.");
			}

		} catch (IllegalArgumentException e) {
			logger.info("Invalid input parameters: " + e.getMessage());
			setErrorMessage(e.getMessage());
		} catch (Exception e) {
			logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
			setErrorMessage("Exception while waiting for the image to appear: " + e.getMessage());
		}

		return result;
	}
}