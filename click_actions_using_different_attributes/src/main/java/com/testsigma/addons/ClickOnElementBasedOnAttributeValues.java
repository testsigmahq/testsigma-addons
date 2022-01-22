package com.testsigma.addons;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
public class ClickOnElementBasedOnAttributeValues extends WebAction {

    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence for the %s with %s%s \"%s\"";
    private static final String SUCCESS_MESSAGE = "Successfully clicked the %s with %s%s \"%s\"";
    private static final String FAILURE_MESSAGE_NODATA = "Found element, But unable to click. <br>" +
            "Please verify if the %s with %s %s <b>%s</b> is enabled";
    private static final String ELEMENT_NOT_FOUND = "Unable to find a %s with %s %s <b>\"%s\"</b>." +
            "<br>If the %s is present in the page, please verify the %s of %s%s given value";
    private String attribute;
    private com.testsigma.sdk.TestData value;
    private String operator;
    private String elementType;


    @Override
    public Result execute() throws NoSuchElementException {
        String elementTypeXpath = "*";
        switch (elementType) {
            case "element":
                elementTypeXpath = "*";
                break;
            case "button":
                elementTypeXpath = "button";
                break;
            case "link":
                elementTypeXpath = "a";
        }
        String xpath;
        if (attribute.equals("text")) {
            xpath = operator.equals("equals") ?
                    "//" + elementTypeXpath + "[text()='" + value.getValue() + "']" :
                    "//" + elementTypeXpath + "[contains(text(),'" + value.getValue() + "')]";
        } else {
            xpath = operator.equals("equals") ?
                    "//" + elementTypeXpath + "[@" + attribute + "='" + value.getValue() + "']" :
                    "//" + elementTypeXpath + "[contains(@" + attribute + ",'" + value.getValue() + "')]";
        }
        return executeSnippet(xpath);
    }

    private Result executeSnippet(String xpath) {
        List<WebElement> elements = driver.findElements(By.xpath(xpath));
        if (elements.size() == 0) {
            setErrorMessage(String.format(
                    ELEMENT_NOT_FOUND,
                    elementType,
                    attribute,
                    (operator.equals("contains") ? "containing" : "equal to"),
                    value.getValue().toString(),
                    attribute,
                    elementType,
                    (operator.equals("contains") ? " contains" : " is equal to"),
                    elementType
            ));
            return Result.FAILED;
        } else if (elements.size() > 1) {
            setErrorMessage(String.format(
                    NOT_UNIQUE_ERROR,
                    elementType,
                    attribute,
                    (operator.equals("contains") ? " containing" : " equal to"),
                    value.getValue().toString()
            ));
            return Result.FAILED;
        }
        WebElement element = elements.get(0);
        if (!element.isEnabled()) {
            setErrorMessage(String.format(
                    FAILURE_MESSAGE_NODATA,
                    elementType,
                    attribute,
                    (operator.equals("contains") ? "containing" : "equal to"),
                    value.getValue().toString()
            ));
            return Result.FAILED;
        }
        element.click();
        setSuccessMessage(String.format(
                SUCCESS_MESSAGE,
                elementType,
                attribute,
                (operator.equals("contains") ? " containing" : " equal to"),
                value.getValue().toString()
        ));
        return Result.SUCCESS;
    }
}

