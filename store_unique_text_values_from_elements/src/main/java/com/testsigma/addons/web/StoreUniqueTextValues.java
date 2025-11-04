package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Action(
        actionText = "Store all unique text values from elements located by element-locator in runtime variable variable-name",
        description = "Fetches text from all elements matching the locator, removes duplicates, and stores unique values in a runtime variable",
        applicationType = ApplicationType.WEB
)
public class StoreUniqueTextValues extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;


    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Starting to extract unique text values from elements...");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            List<WebElement> elements = driver.findElements(element.getBy());

            if (elements.isEmpty()) {
                logger.warn("No elements found for locator: " + element.getBy() + "::" + element.getValue());
                setErrorMessage("No elements found for locator: " + element.getBy() + "::" + element.getValue());
                return com.testsigma.sdk.Result.FAILED;
            }

            Set<String> uniqueTexts = new LinkedHashSet<>();
            for (WebElement el : elements) {
                String text = el.getText().trim();
                if (!text.isEmpty()) {
                    uniqueTexts.add(text);
                }
            }

            String storedValue = String.join(",", uniqueTexts);
            runTimeData.setValue(storedValue);
            runTimeData.setKey(variableName.getValue().toString());


            logger.info("Stored unique text values: " + storedValue);
            setSuccessMessage("Successfully stored unique text values: " + storedValue);
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Failed to store unique text values due to error: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to store unique text values from element . Error: " + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}


