package com.testsigma.addons.windows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;

import org.openqa.selenium.NoSuchElementException;


@Action(actionText = "Print testdata",
description = "Printing the data in console and result",
applicationType = ApplicationType.WINDOWS,
useCustomScreenshot = false)
public class Printtesdata extends WindowsAction {

	@TestData(reference = "testdata")
	private com.testsigma.sdk.TestData testData;


	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		logger.info("testdata" + " = " + testData.getValue().toString());
		setSuccessMessage(testData.getValue().toString());
		logger.info(testData.getValue().toString());
		return result;
	}
}