package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Base64;

@Data
@Action(actionText = "Decode the given base64-string from base64 and store in variable variable-name",
		description = "Decodes the input base64 string back to original",
		applicationType = ApplicationType.MOBILE_WEB,
		useCustomScreenshot = false)
public class DecodeStringToBase64 extends WebAction {

	@TestData(reference = "base64-string")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "variable-name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData2;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {
			String decodedString = new String(Base64.getDecoder().decode(testData1.getValue().toString()));
			logger.info("Decoded string is: " + decodedString);
			logger.info("Variable name is: " + testData2.getValue().toString());
			runTimeData.setKey(testData2.getValue().toString());
			runTimeData.setValue(decodedString);
			logger.info("Successfully stored decoded string in runtime variable: " + testData2.getValue().toString() + " = " + decodedString);
			setSuccessMessage("Successfully decoded base64 string and stored in variable: " + testData2.getValue().toString() + " = " + decodedString);
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Failed to decode base64 string. Error: " + errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}
