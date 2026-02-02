package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Add character test-data1 to the String test-data2 from end position ( End ) and store into testdata", description = "Adds the specified character to the string at the given position from the end and stores it in runtime data", applicationType = ApplicationType.REST_API, useCustomScreenshot = false)
public class AddCharacterToStringByPassingEndPosition extends RestApiAction {

	@TestData(reference = "test-data1")
	private com.testsigma.sdk.TestData character;

	@TestData(reference = "test-data2")
	private com.testsigma.sdk.TestData inputString;

	@TestData(reference = "End")
	private com.testsigma.sdk.TestData positionFromEnd;

	@TestData(reference = "testdata", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testdata;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		logger.info("Initiating execution");
		logger.debug("Input String: " + this.inputString.getValue() + ", Character: " + this.character.getValue()
				+ ", Position From End: " + this.positionFromEnd.getValue());

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		try {
			String input = inputString.getValue().toString();
			String charToAdd = character.getValue().toString();
			int position = Integer.parseInt(positionFromEnd.getValue().toString());

			if (position < 0 || position > input.length()) {
				throw new IllegalArgumentException("Position is out of bounds");
			}

			StringBuilder modifiedString = new StringBuilder(input);
			modifiedString.insert(input.length() - position, charToAdd);

			String resultString = modifiedString.toString();
			runTimeData.setValue(resultString);
			runTimeData.setKey(testdata.getValue().toString());

			setSuccessMessage("Added character '" + charToAdd + "' to the string" + inputString.getValue().toString()
					+ " at position " + position + " from the end and stored the result: " + resultString);

		} catch (NoSuchElementException e) {
			result = com.testsigma.sdk.Result.FAILED;
			logger.info("No such element exception: " + e.getMessage());
			setErrorMessage("No such element exception: " + e.getMessage());
		} catch (Exception e) {
			result = com.testsigma.sdk.Result.FAILED;
			logger.info("An error occurred: " + e.getMessage());
			setErrorMessage("An error occurred: " + e.getMessage());
		}

		return result;
	}
}
