package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove the no of (first,last) positions of given string testdata and store into runtime variable runtime_testdata",
description = "THis action removes the no of first,last position of given string and store into a varaible",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class RemovePositions extends WebAction {

	@TestData(reference = "first,last")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "testdata")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "runtime_testdata", isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {
			String inputString = testData2.getValue().toString();
			String[] Split = testData1.getValue().toString().split(",");
			String first = Split[0]; 
			String last = Split[1];
			if (inputString.length() >= 2) {
				String resultstring = inputString.substring(Integer.parseInt(first), inputString.length() - Integer.parseInt(last));
				
				runTimeData.setKey(testData3.getValue().toString());
				runTimeData.setValue(resultstring);
				
				setSuccessMessage("String after removing first and last letters:" + resultstring +":is stored in the runtime variable:" +testData3.getValue().toString());
				logger.info("String after removing first and last letters:" + resultstring +":is stored in the runtime variable:" +testData3.getValue().toString());

			} else {
				result = com.testsigma.sdk.Result.FAILED;
				logger.warn("Input string must have at least two characters.");
				setErrorMessage("Input string must have at least two characters.");
			}
		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		} 
		return result;
	}
}