package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;


@Action(actionText = "Print testdata",
description = "Printing the data in console and result",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class Printtesdata extends WebAction {

	@TestData(reference = "testdata")
	private com.testsigma.sdk.TestData testData;


	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		setSuccessMessage(testData.getValue().toString());
		logger.info(testData.getValue().toString());
		return result;
	}
}