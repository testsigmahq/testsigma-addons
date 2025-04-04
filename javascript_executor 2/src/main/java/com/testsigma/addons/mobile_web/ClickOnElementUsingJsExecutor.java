package com.testsigma.addons.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Action(actionText = "click on the element element-locator using javascript executor",
        description = "clicks on the given element using javascript executor",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class ClickOnElementUsingJsExecutor extends WebAction {
    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Element locator: "+ this.element.getValue() +" by:"+ this.element.getBy() );
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        WebElement webElement = element.getElement();
        try {
            JavascriptExecutor js = (JavascriptExecutor) appiumDriver;
            js.executeScript("arguments[0].click();", webElement);
        } catch (Exception e) {
            logger.debug("Exception occurred : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred : " + ExceptionUtils.getStackTrace(e));
        }
        setSuccessMessage("Successfully clicked on element using javascript executor");
        return result;
    }
}
