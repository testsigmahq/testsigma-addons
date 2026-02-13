package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.ApplicationType;

import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.WebAction;
import lombok.Data;

import java.util.WeakHashMap;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.testsigma.sdk.Result;

@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Action(actionText = "Verify if element element-locator is element-state with scrollable boolean-value",
        description = "verifies if the element is in the given state",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class VerifyElementPresenceWithScroll extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "element-state", allowedValues = {"CHECKED", "UNCHECKED", "PRESENT", "NOT PRESENT", "ENABLED", "DISABLED", "DISPLAYED", "NOT DISPLAYED"})
    private com.testsigma.sdk.TestData elementState;
    @TestData(reference = "boolean-value", allowedValues = {"TRUE", "FALSE"})
    private com.testsigma.sdk.TestData scrollable;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Element locator: " + this.element.getValue() + " by: " + this.element.getBy());
        try {
            WebElement webElement = element.getElement();
            logger.info("element found");
            String scrollableValue = scrollable.getValue().toString().toLowerCase();
            if ("true".equalsIgnoreCase(scrollableValue)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
            }
            String elementStateValue = elementState.getValue().toString().toLowerCase();
            logger.info("element state value: " + elementStateValue);
            switch (elementStateValue) {
                case "checked":
                    if (webElement.isSelected()) {
                        logger.info("Element is checked");
                        result = Result.SUCCESS;
                    } else {
                        logger.info("Element is not checked");
                        result = Result.FAILED;
                    }
                    break;
                case "unchecked":
                    if (webElement.isSelected()) {
                        logger.info("Element is checked");
                        result = Result.FAILED;
                    } else {
                        logger.info("Element is not checked");
                        result = Result.SUCCESS;
                    }
                    break;
                case "present":
                    if (webElement.isDisplayed()) {
                        logger.info("Element is displayed");
                        result = Result.SUCCESS;
                    } else {
                        logger.info("Element is not displayed");
                        result = Result.FAILED;
                    }
                    break;
                case "not present":
                    if (webElement.isDisplayed()) {
                        logger.info("Element is displayed");
                        result = Result.FAILED;
                    } else {
                        logger.info("Element is not displayed");
                        result = Result.SUCCESS;
                    }
                    break;
                case "enabled":
                    if (webElement.isEnabled()) {
                        logger.info("Element is enabled");
                        result = Result.SUCCESS;
                    } else {
                        logger.info("Element is not enabled");
                        result = Result.FAILED;
                    }
                    break;
                case "disabled":
                    if (webElement.isEnabled()) {
                        logger.info("Element is enabled");
                        result = Result.FAILED;
                    } else {
                        logger.info("Element is not enabled");
                        result = Result.SUCCESS;
                    }
                    break;
                case "displayed":
                    if (webElement.isDisplayed()) {
                        logger.info("Element is displayed");
                        result = Result.SUCCESS;
                    } else {
                        logger.info("Element is not displayed");
                        result = Result.FAILED;
                    }
                    break;
                case "not displayed":
                    if (webElement.isDisplayed()) {
                        logger.info("Element is displayed");
                        result = Result.FAILED;
                    } else {
                        logger.info("Element is not displayed");
                        result = Result.SUCCESS;
                    }
                    break;
                default:
                    result = Result.FAILED;
                    logger.warn("Invalid element state: " + elementStateValue);
                    setErrorMessage(String.format("Invalid element state: %s", elementStateValue));
                    break;
            }
        } catch (NoSuchElementException e) {
            result = Result.FAILED;
            logger.warn("Element not found with locator: " + this.element.getBy() + "::" + this.element.getValue());
            setErrorMessage(String.format("Element not found with locator %s::%s", this.element.getBy(), this.element.getValue()));
        } catch (Exception e) {
            result = Result.FAILED;
            logger.warn("Error verifying element state: " + e.getMessage());
            setErrorMessage(String.format("Error verifying element state: %s", e.getMessage()));
        }
        return result;
    }
}
