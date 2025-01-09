package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Action(actionText = "Verify that the element has non-empty value",
		description = "This addon will verify that the element contains any value for attribute",
		applicationType = ApplicationType.ANDROID,
		useCustomScreenshot = false)

public class VerifyElementNonEmpty extends AndroidAction {

	@Element(reference = "element")
	private com.testsigma.sdk.Element element;

	@Override
	protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
		// Your Awesome code starts here
		logger.info("Initiating execution");
		logger.debug("Element: " + this.element.getValue() + " by:" + this.element.getBy());

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		AndroidDriver androidDriver = (AndroidDriver) this.driver;

		WebElement webElement = element.getElement();
		try {
			if (webElement.getText().isEmpty()) {
				result = com.testsigma.sdk.Result.FAILED;
				setErrorMessage("The text field is empty.");
			} else {
				setSuccessMessage(String.format("The text field has a text "+webElement.getText()+". it's not empty.", element.getBy()));
			}
		} catch (Exception e) {
			result = com.testsigma.sdk.Result.FAILED;
			logger.warn("Element with locator %s is not visible ::" + element.getBy() + "::" + element.getValue());
		}
		return result;
	}
}