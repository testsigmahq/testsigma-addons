package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.Duration;

@Action(
        actionText = "Tap and hold on element element-locator for time-in-s seconds",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false
)
public class TapAndHoldOnElementForDuration extends AndroidAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "time-in-s")
    private com.testsigma.sdk.TestData timeInSeconds;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Starting dynamic tap and hold action...");

        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        WebElement webElement = element.getElement();

        try {
            int holdSeconds = Integer.parseInt(timeInSeconds.getValue().toString());

            if (holdSeconds < 0) {
                logger.warn("Hold duration received less than 0");
                setErrorMessage("Hold duration received less than 0");
                result = com.testsigma.sdk.Result.FAILED;
                return result;
            }

            logger.info("Hold duration received: " + holdSeconds + " seconds");

            if (webElement.isDisplayed()) {
                logger.info("Element is visible, performing long press for " + holdSeconds + " seconds...");

                TouchAction action = new TouchAction(androidDriver);
                action.longPress(ElementOption.element(webElement))
                        .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(holdSeconds)))
                        .release()
                        .perform();

                setSuccessMessage(String.format(
                        "Successfully performed tap and hold for %d seconds on element with %s::%s",
                        holdSeconds, element.getBy(), element.getValue()
                ));
            } else {
                result = com.testsigma.sdk.Result.FAILED;
                logger.warn("Element is not visible");
                setErrorMessage("Element is not visible");
                return result;
            }

        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Error during tap and hold action: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to perform tap and hold due to: " + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}

