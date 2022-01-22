package com.testsigma.addons.verify.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
public class VerifyThatPageDisplaysElementsWithAttributes extends WebAction {

    private static final String SUCCESS_MESSAGE = "Element has the expected Text";
    private static final String ELEMENT_NOT_FOUND_ERROR_MESSAGE = "Expected Element Not Found";
    private static final String ELEMENT_NOT_DISPLAYED = "Element is not displayed";
    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence of the Element";
    private String elementType;
    private String attribute;
    private com.testsigma.sdk.TestData value;
    private String operator;

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
                break;
            case "img":
                elementTypeXpath = "img";
                break;
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
        if (elements.isEmpty()) {
            setErrorMessage(ELEMENT_NOT_FOUND_ERROR_MESSAGE);
            return Result.FAILED;
        } else if (elements.size() > 1) {
            setErrorMessage(NOT_UNIQUE_ERROR);
            return Result.FAILED;
        } else if (elements.get(0).isDisplayed()) {
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        } else {
            setErrorMessage(ELEMENT_NOT_DISPLAYED);
            return Result.FAILED;
        }
    }
}

