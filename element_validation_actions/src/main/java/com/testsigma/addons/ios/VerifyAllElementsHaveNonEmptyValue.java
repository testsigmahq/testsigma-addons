package com.testsigma.addons.ios;

import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Action(actionText = "Verify all elements with locator element-locator have non-empty values",
        description = "Finds all elements matching the given locator and verifies that each element has a non-empty value",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class VerifyAllElementsHaveNonEmptyValue extends IOSAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Element locator: " + this.element.getValue() + " by: " + this.element.getBy());
        
        IOSDriver iosDriver = (IOSDriver) this.driver;
        
        // Find all elements matching the locator
        List<WebElement> elements = iosDriver.findElements(this.element.getBy());
        
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
            } catch (Exception e) {
                logger.warn("Error getting value for element at index " + i + ": " + e.getMessage());
            }
            
            // Check if value is empty
            if (elementValue == null || elementValue.trim().isEmpty()) {
                setErrorMessage(String.format("Element at index %d with locator %s::%s has an empty value. Found %d total elements.",
                        i, this.element.getBy(), this.element.getValue(), elements.size()));
                result = com.testsigma.sdk.Result.FAILED;
                logger.warn("Element at index " + i + " has empty value. Locator: " + this.element.getBy() + "::" + this.element.getValue());
                return result;
            } else {
                logger.debug("Element at index " + i + " has value: " + elementValue);
            }
        }

        logger.info("Successfully verified that all elements with locator " + this.element.getBy() + "::" + this.element.getValue() + " have non-empty values");
        setSuccessMessage(String.format("Successfully verified that all %d elements with locator %s::%s have non-empty values",
                elements.size(), this.element.getBy(), this.element.getValue()));
        return result;
    }
}

