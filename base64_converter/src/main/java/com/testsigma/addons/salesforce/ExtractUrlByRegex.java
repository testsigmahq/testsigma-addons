package com.testsigma.addons.salesforce;

import com.testsigma.sdk.SalesforceAction;
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
@Action(actionText = "Extract URLs matching regex from input-string and store in variable variable-name",
		description = "Extracts all URLs from the input string that match the given regex and returns them comma-separated",
		applicationType = ApplicationType.Salesforce,
		useCustomScreenshot = false)
public class ExtractUrlByRegex extends SalesforceAction {

	private static final Pattern URL_PATTERN = Pattern.compile(
			"(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);

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
			Pattern userPattern = Pattern.compile(testData2.getValue().toString());

			List<String> matchingUrls = new ArrayList<>();
			Matcher urlMatcher = URL_PATTERN.matcher(input);
			while (urlMatcher.find()) {
				String url = urlMatcher.group(1);
				if (userPattern.matcher(url).find()) {
					matchingUrls.add(url);
				}
			}
			if (matchingUrls.isEmpty()) {
				logger.info("No URLs matching the given regex were found in the provided string");
				result = com.testsigma.sdk.Result.FAILED;
				setErrorMessage("No URLs matching the given regex were found in the provided string");
			} else {
				String matchResult = String.join(",", matchingUrls);
				logger.info("Matching URLs found: " + matchResult);
				logger.info("Variable name is: " + testData3.getValue().toString());
				runTimeData.setKey(testData3.getValue().toString());
				runTimeData.setValue(matchResult);
				logger.info("Successfully stored extracted URLs in runtime variable: " + testData3.getValue().toString() + " = " + matchResult);
				setSuccessMessage("Extracted URLs stored in variable: " + testData3.getValue().toString() + " = " + matchResult);
			}
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Failed to extract URLs from string. Error: " + errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}
