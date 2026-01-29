package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

@Data
@Action(actionText = "Replace string1 with string2 in the given text String3 and store into testdata", description = "Replaces all occurrences of string1 with string2 in the text and stores the modified text into a runtime variable", applicationType = ApplicationType.REST_API, useCustomScreenshot = false)
public class StringReplaceAction extends RestApiAction {

	@TestData(reference = "string1")
	private com.testsigma.sdk.TestData string1TestData;

	@TestData(reference = "string2")
	private com.testsigma.sdk.TestData string2TestData;

	@TestData(reference = "String3")
	private com.testsigma.sdk.TestData textTestData;

	@TestData(reference = "testdata", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() {
		logger.info("Initiating execution for string replacement");
		logger.debug("Text: " + this.textTestData.getValue() + ", String1: " + this.string1TestData.getValue()
				+ ", String2: " + this.string2TestData.getValue());

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		try {
			String text = this.textTestData.getValue().toString();
			String string1 = this.string1TestData.getValue().toString();
			String string2 = this.string2TestData.getValue().toString();

			logger.info("Original Text: " + text);
			logger.info("String to be replaced: " + string1);
			logger.info("Replacement String: " + string2);

			String modifiedText = text.replaceAll(string1, string2);

			logger.info("Modified Text: " + modifiedText);

			runTimeData.setValue(modifiedText);
			runTimeData.setKey(testData.getValue().toString());
			setSuccessMessage("Successfully replaced all occurrences of '" + string1 + "' with '" + string2
					+ "' in the text : " + modifiedText);
		} catch (Exception e) {
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Error occurred while replacing string: " + e.getMessage());
			logger.warn("Exception: " + e);
		}
		return result;
	}
}
