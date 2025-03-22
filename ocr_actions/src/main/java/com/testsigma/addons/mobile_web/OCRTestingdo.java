package com.testsigma.addons.mobile_web;

import com.google.common.collect.ImmutableMap;
import com.testsigma.addons.utils.ContextUtils;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DriverCommand;

import java.io.File;

import static com.testsigma.addons.utils.ClickActionUtils.performClickText;


@Data
@Action(actionText = "Do click on the element by exacttext",
		description = "Using OCR do the element click",
		applicationType = ApplicationType.MOBILE_WEB)
public class OCRTestingdo extends AndroidAction {
	@OCR
	private com.testsigma.sdk.OCR ocr;

	@TestData(reference = "exacttext")
	private com.testsigma.sdk.TestData text;

	@TestStepResult
	private com.testsigma.sdk.TestStepResult testStepResult;

	@Override
	protected Result execute() {
		AppiumDriver appiumDriver = (AppiumDriver) driver;
		Result result = Result.SUCCESS;
		String existingContext = ContextUtils.getCurrentContext(appiumDriver);
		try {
			TakesScreenshot scrShot = ((TakesScreenshot) driver);
			File baseImageFile = scrShot.getScreenshotAs(OutputType.FILE);
			String url = testStepResult.getScreenshotUrl();
			logger.info("Amazon s3 url in which we are storing base image"+url);
			ocr.uploadFile(url, baseImageFile);

			// Switch to native app context
			appiumDriver.execute(DriverCommand.SWITCH_TO_CONTEXT, ImmutableMap.of("name", "NATIVE_APP"));

			String successMessage = performClickText(ocr, text.getValue().toString(), baseImageFile, appiumDriver, logger);

			setSuccessMessage(successMessage);

			// Switch back to old context
			appiumDriver.execute(DriverCommand.SWITCH_TO_CONTEXT, ImmutableMap.of("name", existingContext));
		} catch (Exception e) {
			appiumDriver.execute(DriverCommand.SWITCH_TO_CONTEXT, ImmutableMap.of("name", existingContext));
			logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
			setErrorMessage("Exception occurred while performing click on image: " + e.getMessage());
			return Result.FAILED;
		}
		return result;
	}
}