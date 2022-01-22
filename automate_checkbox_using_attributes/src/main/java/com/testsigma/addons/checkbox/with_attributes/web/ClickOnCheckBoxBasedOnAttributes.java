package com.testsigma.addons.checkbox.with_attributes.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
public class ClickOnCheckBoxBasedOnAttributes extends WebAction {

    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence for the checkbox with %s%s \"%s\"";
    private static final String SUCCESS_MESSAGE = "Successfully clicked the checkbox with %s%s \"%s\"";
    private static final String FAILURE_MESSAGE_NODATA = "Found element, But unable to click. <br>" +
            "Please verify if the checkbox with %s %s <b>%s</b> is enabled and accepting data.";
    private static final String ELEMENT_NOT_FOUND = "Unable to find a checkbox with %s%s <b>\"%s\"</b>." +
            "<br>If the checkbox is present in the page, please verify the %s of checkbox%s given value";
    private String attribute;
    private com.testsigma.sdk.TestData testData;
    private String operator;
    private Boolean isLabelText;
    private Boolean isPrecedingText;
    private Boolean isFollowingText;
    private Boolean isContainerLabel;

    @Override
    public Result execute() throws NoSuchElementException {
        String xpath;
        if (isLabelText.equals(true)) {
            xpath = operator.equals("equals") ?
                    (isContainerLabel != null ?
                            "//label[text()='" + testData.getValue() + "']//following::input[@type='checkbox'][1]"
                            : (isFollowingText != null ?
                            "//label[text()='" + testData.getValue() + "']//preceding::input[@type='checkbox'][1]" :
                            "//label[text()='" + testData.getValue() + "']//following::input[@type='checkbox'][1]"))
                    : (isContainerLabel != null ?
                    "//label//text()[contains(.,'" + testData.getValue() + "')]//following::input[@type='checkbox'][1]"
                    : (isFollowingText != null ?
                    "//label[contains(text(),'" + testData.getValue() + "')]//preceding::input[@type='checkbox'][1]" :
                    "//label[contains(text(),'" + testData.getValue() + "')]//following::input[@type='checkbox'][1]"));
        } else {
            if (attribute.equals("text")) {
                xpath = operator.equals("equals") ?
                        (isFollowingText != null && isFollowingText.equals(true) ?
                                "//*[text()='" + testData.getValue() + "']//preceding::input[@type='checkbox'][1]" :
                                "//*[text()='" + testData.getValue() + "']//following::input[@type='checkbox'][1]")
                        :
                        (isFollowingText != null && isFollowingText.equals(true) ?
                                "//*[contains(text(),'" + testData.getValue() + "')]//preceding::input[@type='checkbox'][1]" :
                                "//*[contains(text(),'" + testData.getValue() + "')]//following::input[@type='checkbox'][1]"
                        );
            } else {
                xpath = operator.equals("equals") ?
                        "//input[@type='checkbox' and @" + attribute + "='" + testData.getValue() + "']" :
                        "//input[@type='checkbox' and contains(@" + attribute + ",'" + testData.getValue() + "')]";
            }
        }

        return executeSnippet(xpath);
    }

    private Result executeSnippet(String xpath) {
        List<WebElement> elements = driver.findElements(By.xpath(xpath));
        String attributeString = (isLabelText ? "" : (isFollowingText != null ? "following " : (isPrecedingText != null ? "preceding " : ""))) + attribute;
        String operatorString = ((isLabelText ?
                (isContainerLabel != null ? " of label " :
                        (isFollowingText != null ? " of following label " : " of preceding label ")) : " ")
                + (operator.equals("contains") ? "containing" : "equal to"));
        String operatorString1 = ((isLabelText ?
                (isContainerLabel != null ? "'s label " :
                        (isFollowingText != null ? "'s following label " : "'s preceding label ")) : " ")
                + (operator.equals("contains") ? "contains" : "is equal to"));
        if (elements.size() == 0) {
            setErrorMessage(String.format(
                    ELEMENT_NOT_FOUND,
                    attributeString,
                    operatorString,
                    testData.getValue().toString(),
                    attributeString,
                    operatorString1
            ));
            return Result.FAILED;
        } else if (elements.size() > 1) {
            setErrorMessage(String.format(
                    NOT_UNIQUE_ERROR,
                    attributeString,
                    operatorString,
                    testData.getValue().toString()
            ));
            return Result.FAILED;
        }
        WebElement element = elements.get(0);
        if (!element.isEnabled()) {
            setErrorMessage(String.format(
                    FAILURE_MESSAGE_NODATA,
                    attributeString,
                    operatorString,
                    testData.getValue().toString()
            ));
            return Result.FAILED;
        }
        element.click();
        setSuccessMessage(String.format(
                SUCCESS_MESSAGE,
                attributeString,
                operatorString,
                testData.getValue().toString()
        ));
        return Result.SUCCESS;
    }
}

