package com.testsigma.addons.windowslite;

import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "Extract values matching regex from input-string and store in variable variable-name",
		description = "Extracts all values matching the given regex from the input string and returns them comma-separated",
		applicationType = ApplicationType.WINDOWS,
		useCustomScreenshot = false)
public class ExtractUrlsFromString extends WindowsAction {

	@TestData(reference = "input-string")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "regex")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "variable-name", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {
			String input = testData1.getValue().toString();
			logger.info("Input string is: " + input);
			logger.info("Regex pattern is: " + testData2.getValue().toString());
			Pattern pattern = Pattern.compile(testData2.getValue().toString());
			Matcher matcher = pattern.matcher(input);
			List<String> matches = new ArrayList<>();
			while (matcher.find()) {
				matches.add(matcher.group());
			}
			if (matches.isEmpty()) {
				logger.info("No matches found for the given regex in the provided string");
				result = com.testsigma.sdk.Result.FAILED;
				setErrorMessage("No matches found for the given regex in the provided string");
			} else {
				String matchResult = String.join(",", matches);
				logger.info("Matches found: " + matchResult);
				logger.info("Variable name is: " + testData3.getValue().toString());
				runTimeData.setKey(testData3.getValue().toString());
				runTimeData.setValue(matchResult);
				logger.info("Successfully stored extracted values in runtime variable: " + testData3.getValue().toString() + " = " + matchResult);
				setSuccessMessage("Extracted values stored in variable: " + testData3.getValue().toString() + " = " + matchResult);
			}
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Failed to extract values from string. Error: " + errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}
