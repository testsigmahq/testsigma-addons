package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Store the list of attribute attribute-value from the element element-locator into a runtime variable variable-name with the separator separator-value",
        description = "Stores values from all elements matching a locator into a runtime variable joined by a custom separator. Use 'text' as the attribute to collect visible text, or specify any HTML attribute name (e.g. href, data-id, value).",
        applicationType = ApplicationType.WEB)
public class StoreElementValueListAction extends WebAction {

    @TestData(reference = "attribute-value")
    private com.testsigma.sdk.TestData attribute;

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable;

    @TestData(reference = "separator-value")
    private com.testsigma.sdk.TestData separator;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try {
            String attributeName = attribute.getValue().toString().trim();
            String variableName  = variable.getValue().toString();
            String sep           = separator.getValue() != null ? separator.getValue().toString() : ",";

            List<WebElement> elements = driver.findElements(element.getBy());

            if (elements.isEmpty()) {
                logger.warn("No elements found for locator: " + element.getValue());
                setErrorMessage("No elements found for locator: " + element.getValue());
                return com.testsigma.sdk.Result.FAILED;
            }

            boolean useText = attributeName.equalsIgnoreCase("text");

            String joined = elements.stream()
                    .map(el -> {
                        if (useText) {
                            return el.getText();
                        }
                        String val = el.getAttribute(attributeName);
                        return val != null ? val : "";
                    })
                    .collect(Collectors.joining(sep));

            logger.info("Attribute: " + attributeName + " | Element count: " + elements.size() + " | Separator: '" + sep + "'");
            logger.info("Collected values: " + joined);

            runTimeData.setKey(variableName);
            runTimeData.setValue(joined);

            setSuccessMessage("Stored " + elements.size() + " value(s) into '" + variableName + "' | Values: " + joined);
            return com.testsigma.sdk.Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception in StoreElementValueListAction: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error: " + ExceptionUtils.getMessage(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
