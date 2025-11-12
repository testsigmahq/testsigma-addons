package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

@Action(actionText = "Verify all elements with locator element-locator have non-empty values",
        description = "Finds all elements matching the given locator and verifies that each element has a non-empty value",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyAllElementsHaveNonEmptyValue extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Element locator: " + this.element.getValue() + " by: " + this.element.getBy());
        
        WebDriver webDriver = this.driver;
        
        // Find all elements matching the locator
        List<WebElement> elements = webDriver.findElements(this.element.getBy());
        
        if (elements.isEmpty()) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("No elements found with locator: " + this.element.getBy() + "::" + this.element.getValue());
            setErrorMessage(String.format("No elements found with locator %s::%s", this.element.getBy(), this.element.getValue()));
            return result;
        }
        
        logger.info("Found " + elements.size() + " elements matching the locator");
        
        // Check each element for non-empty value
        int emptyElementIndex = -1;
        for (int i = 0; i < elements.size(); i++) {
            WebElement webElement = elements.get(i);
            String elementValue = null;
            
            // Try to get text value - check both getText() and getAttribute("text")
            try {
                elementValue = webElement.getText();
                if (elementValue == null || elementValue.trim().isEmpty()) {
                    // Try getAttribute("text") as fallback
                    elementValue = webElement.getAttribute("text");
                }
                if (elementValue == null || elementValue.trim().isEmpty()) {
                    // Try getAttribute("value") as another fallback
                    elementValue = webElement.getAttribute("value");
                }
                if (elementValue == null || elementValue.trim().isEmpty()) {
                    // Try getAttribute("innerText") as another fallback
                    elementValue = webElement.getAttribute("innerText");
                }
            } catch (Exception e) {
                logger.warn("Error getting value for element at index " + i + ": " + e.getMessage());
            }
            
            // Check if value is empty
            if (elementValue == null || elementValue.trim().isEmpty()) {
                emptyElementIndex = i;
                result = com.testsigma.sdk.Result.FAILED;
                logger.warn("Element at index " + i + " has empty value. Locator: " + this.element.getBy() + "::" + this.element.getValue());
                break;
            } else {
                logger.debug("Element at index " + i + " has value: " + elementValue);
            }
        }
        
        if (result == com.testsigma.sdk.Result.SUCCESS) {
            logger.info("Successfully verified that all elements with locator " + this.element.getBy() + "::" + this.element.getValue() + " have non-empty values");
            setSuccessMessage(String.format("Successfully verified that all %d elements with locator %s::%s have non-empty values", 
                    elements.size(), this.element.getBy(), this.element.getValue()));
        } else {
            logger.warn("Failed to verify that all elements with locator " + this.element.getBy() + "::" + this.element.getValue() + " have non-empty values");
            setErrorMessage(String.format("Element at index %d with locator %s::%s has an empty value. Found %d total elements.", 
                    emptyElementIndex, this.element.getBy(), this.element.getValue(), elements.size()));
            result = com.testsigma.sdk.Result.FAILED;
        }
        logger.info("Execution completed");
        return result;
    }
}

