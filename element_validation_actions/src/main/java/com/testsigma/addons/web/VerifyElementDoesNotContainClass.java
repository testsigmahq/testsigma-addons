package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

@Action(actionText = "Verify that the element element-locator does not contain the class test-data with scrollable bool-value",
        description = "Verifies that the target element does not contain the specified CSS class in its class attribute",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyElementDoesNotContainClass extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "bool-value", allowedValues = {"true", "false"})
    private com.testsigma.sdk.TestData boolValue;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");

        try {
            WebElement webElement = driver.findElement(this.element.getBy());

            if ("true".equalsIgnoreCase(boolValue.getValue().toString().trim())) {
                logger.info("Scrolling element into view");
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
            }

            String classAttribute = webElement.getAttribute("class");
            String expectedAbsentClass = testData.getValue().toString().trim();

            logger.info("Element class attribute: " + classAttribute);
            logger.info("Class expected to be absent: " + expectedAbsentClass);

            if (classAttribute == null) {
                setSuccessMessage(String.format(
                        "Successfully verified that element %s::%s does not contain class \"%s\" (class attribute is absent)",
                        this.element.getBy(), this.element.getValue(), expectedAbsentClass));
                return com.testsigma.sdk.Result.SUCCESS;
            }

            List<String> actualClasses = Arrays.asList(classAttribute.split("\\s+"));

            if (actualClasses.contains(expectedAbsentClass)) {
                setErrorMessage(String.format(
                        "Verification failed: element %s::%s contains the class \"%s\" but it should not. Actual class attribute: \"%s\"",
                        this.element.getBy(), this.element.getValue(), expectedAbsentClass, classAttribute));
                return com.testsigma.sdk.Result.FAILED;
            }

            setSuccessMessage(String.format(
                    "Successfully verified that element %s::%s does not contain class \"%s\". Actual class attribute: \"%s\"",
                    this.element.getBy(), this.element.getValue(), expectedAbsentClass, classAttribute));
            return com.testsigma.sdk.Result.SUCCESS;

        } catch (NoSuchElementException e) {
            logger.warn("Element not found with locator " + this.element.getBy() + "::" + this.element.getValue() + " - " + e.getMessage());
            setErrorMessage(String.format(
                    "Element not found with locator %s::%s. Please verify the element locator is correct.",
                    this.element.getBy(), this.element.getValue()));
            return com.testsigma.sdk.Result.FAILED;
        } catch (ClassCastException e) {
            logger.warn("Element not found (ClassCastException) for locator " + this.element.getBy() + "::" + this.element.getValue() + " - " + e.getMessage());
            setErrorMessage(String.format(
                    "Element not found with locator %s::%s. The element could not be located on the page.",
                    this.element.getBy(), this.element.getValue()));
            return com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            logger.warn("Unexpected error during execution: " + e.getMessage());
            setErrorMessage(String.format(
                    "Operation failed for element %s::%s: %s",
                    this.element.getBy(), this.element.getValue(), e.getMessage()));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
