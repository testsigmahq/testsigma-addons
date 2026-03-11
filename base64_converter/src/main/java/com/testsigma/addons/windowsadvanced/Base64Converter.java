package com.testsigma.addons.windowsadvanced;

import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Base64;

@Data
@Action(actionText = "Convert the given string to base64 and store in variable variable-name",
		description = "Converts the input string to base64",
		applicationType = ApplicationType.WINDOWS_ADVANCED,
		useCustomScreenshot = false)
public class Base64Converter extends WindowsAdvancedAction {

	@TestData(reference = "string")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "variable-name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData2;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {
			String base64String = Base64.getEncoder().encodeToString(testData1.getValue().toString().getBytes());
			logger.info("Base64 encoded string is: " + base64String);
			logger.info("Variable name is: " + testData2.getValue().toString());
			runTimeData.setKey(testData2.getValue().toString());
			runTimeData.setValue(base64String);
			logger.info("Successfully stored base64 string in runtime variable: " + testData2.getValue().toString() + " = " + base64String);
			setSuccessMessage("Successfully converted string to base64 and stored in variable: " + testData2.getValue().toString() + " = " + base64String);
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Failed to convert string to base64. Error: " + errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}
