package com.testsigma.addons.enter_with_attributes.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
public class EnterInElementBasedOnAttributes extends WebAction {

    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence for the element with %s%s \"%s\"";
    private static final String SUCCESS_MESSAGE = "Successfully entered data \"%s\" into the element with %s%s \"%s\"";
    private static final String FAILURE_MESSAGE_NODATA = "Found element, But unable to enter given test data <b>\"%s\"</b>. <br>" +
            "Please verify if the element with %s%s <b>%s</b> is enabled and accepting data.";
    private static final String ELEMENT_NOT_FOUND = "Unable to find an element with %s%s <b>\"%s\"</b>." +
            "<br>If the element is present in the page, please verify the %s of element%s given value";
    private com.testsigma.sdk.TestData testData;
    private String attribute;
    private com.testsigma.sdk.TestData value;
    private String operator;
    private Boolean isLabelText;
    private Boolean isFollowingText;
    private Boolean isElementType;
    private Boolean isContainerLabel;

    @Override
    public Result execute() throws NoSuchElementException {
        String xpath;
        if (isLabelText.equals(true)) {
            xpath = operator.equals("equals") ?
                    (isContainerLabel != null ?
                            "//label[text()='" + value.getValue() + "']//following::input[1]"
                            : (isFollowingText != null ?
                            "//label[text()='" + value.getValue() + "']//preceding::input[1]" :
                            "//label[text()='" + value.getValue() + "']//following::input[1]"))
                    : (isContainerLabel != null ?
                    "//label//text()[contains(.,'" + value.getValue() + "')]//following::input[1]"
                    : (isFollowingText != null ?
                    "//label[contains(text(),'" + value.getValue() + "')]//preceding::input[1]" :
                    "//label[contains(text(),'" + value.getValue() + "')]//following::input[1]"));
        } else {
            if (attribute.equals("text")) {
                xpath = operator.equals("equals") ?
                        (isElementType != null && isElementType.equals(true) ?
                                "//*[text()='" + value.getValue() + "']//following::input[1]" :
                                (isFollowingText != null && isFollowingText.equals(true) ?
                                        "//*[text()='" + value.getValue() + "']//preceding::input[1]" :
                                        "//*[text()='" + value.getValue() + "']//following::input[1]")
                        ) :
                        (isElementType != null && isElementType.equals(true) ?
                                "//*[contains(text(),'" + value.getValue() + "')]" :
                                (isFollowingText != null && isFollowingText.equals(true) ?
                                        "//*[contains(text(),'" + value.getValue() + "')]//preceding::input[1]" :
                                        "//*[contains(text(),'" + value.getValue() + "')]//following::input[1]")
                        );
            } else {
                xpath = operator.equals("equals") ?
                        "//*[@" + attribute + "='" + value.getValue() + "']" :
                        "//*[contains(@" + attribute + ",'" + value.getValue() + "')]";
            }
        }

        return executeSnippet(xpath);
    }

    private Result executeSnippet(String xpath) {
        List<WebElement> elements = driver.findElements(By.xpath(xpath));
        String attributeString = (isLabelText ? "" : (isElementType == null ? (isFollowingText != null ? "following " : "") : "")) + attribute;
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
                    value.getValue().toString(),
                    attributeString,
                    operatorString1
            ));
            return Result.FAILED;
        } else if (elements.size() > 1) {
            setErrorMessage(String.format(
                    NOT_UNIQUE_ERROR,
                    attributeString,
                    operatorString,
                    value.getValue().toString()
            ));
            return Result.FAILED;
        }
        WebElement element = elements.get(0);
        if (!element.isEnabled()) {
            setErrorMessage(String.format(
                    FAILURE_MESSAGE_NODATA,
                    testData.getValue().toString(),
                    attributeString,
                    operatorString,
                    value.getValue().toString()
            ));
            return Result.FAILED;
        }
        element.sendKeys(testData.getValue().toString());
        setSuccessMessage(String.format(
                SUCCESS_MESSAGE,
                testData.getValue().toString(),
                attributeString,
                operatorString,
                value.getValue().toString()
        ));
        return Result.SUCCESS;
    }
}
