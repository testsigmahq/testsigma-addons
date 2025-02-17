package com.syslatech.testsigma.addons.web;

import org.openqa.selenium.NoSuchElementException;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;

@Data
@Action(actionText = "Replace character testdata1 in String testdata2 between positions ( start , end ) and store into testdata3", description = "Replaces the specified character to the string between the given start and end positions and stores it in runtime data", applicationType = ApplicationType.WEB, useCustomScreenshot = false)
public class ReplaceCharacterInStringBetweenPositions extends WebAction {

	@TestData(reference = "testdata2")
	private com.testsigma.sdk.TestData inputString;

	@TestData(reference = "testdata1")
	private com.testsigma.sdk.TestData character;

	@TestData(reference = "start")
	private com.testsigma.sdk.TestData startPosition;

	@TestData(reference = "end")
	private com.testsigma.sdk.TestData endPosition;

	@TestData(reference = "testdata3", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testdata;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		logger.info("Initiating execution");
		logger.debug("Input String: " + this.inputString.getValue() + ", Character: " + this.character.getValue()
				+ ", Start Position: " + this.startPosition.getValue() + ", End Position: "
				+ this.endPosition.getValue());

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		try {
			String input = inputString.getValue().toString();
			String charToAdd = character.getValue().toString();
			int start = Integer.parseInt(startPosition.getValue().toString());
			int end = Integer.parseInt(endPosition.getValue().toString());

			if (start < 0 || end > input.length() || start > end) {
				throw new IllegalArgumentException("Positions are out of bounds or invalid");
			}

			String resultString = input.substring(0, start) + charToAdd + input.substring(end);
			runTimeData.setValue(resultString);
			runTimeData.setKey(testdata.getValue().toString());

			setSuccessMessage(
					"Releaced character '" + charToAdd + "' to the string " + inputString.getValue().toString()
							+ "between positions " + start + " and " + end + " and stored the result: " + resultString);

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
