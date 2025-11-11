package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.Result;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(
        actionText = "Clear the existing text from element-locator and enter test-data",
        description = "Clears any existing text in the input field and enters the provided test data.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ClearAndEnterText extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Starting ClearAndEnterText execution");
        
        try {
            WebElement webElement = element.getElement();
            webElement.clear();
            logger.info("Clearing text from element");

            webElement.sendKeys(testData.getValue().toString());
            logger.info("Entered data: " + testData.getValue().toString());
        } catch (Exception e) {
            result = Result.FAILED;
            logger.warn("Exception occurred while clearing text from element: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while clearing text from element: " + ExceptionUtils.getMessage(e));
        }

        setSuccessMessage("Successfully cleared existing text and entered new value: " + testData.getValue());
        return result;
    }
}
