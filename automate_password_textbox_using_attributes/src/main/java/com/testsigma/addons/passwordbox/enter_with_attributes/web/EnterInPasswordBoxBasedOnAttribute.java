package com.testsigma.addons.passwordbox.enter_with_attributes.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
public class EnterInPasswordBoxBasedOnAttribute extends WebAction {

    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence for the password text box with %s%s \"%s\"";
    private static final String SUCCESS_MESSAGE = "Successfully entered data \"%s\" into the password text box with %s%s \"%s\"";
    private static final String FAILURE_MESSAGE_NODATA = "Found element, But unable to enter given test data <b>\"%s\"</b>. <br>" +
            "Please verify if the password text box with %s %s <b>%s</b> is enabled and accepting data.";
    private static final String ELEMENT_NOT_FOUND = "Unable to find a password text box with %s %s <b>\"%s\"</b>." +
            "<br>If the password text box is present in the page, please verify the %s of password text box%s given value";
    private com.testsigma.sdk.TestData testData;
    private String attribute;
    private com.testsigma.sdk.TestData value;
    private String operator;
    private Boolean isLabelText;
    private Boolean isFollowingText;
    private Boolean isPrecedingText;
    private Boolean isContainerLabel;

    @Override
    public Result execute() throws NoSuchElementException {
        String xpath;
        if (isLabelText.equals(true)) {
            xpath = operator.equals("equals") ?
                    (isContainerLabel != null ?
                            "//label[text()='" + value.getValue() + "']//following::input[@type='password'][1]"
                            : (isFollowingText != null && isFollowingText.equals(true) ?
                            "//label[text()='" + value.getValue() + "']//preceding::input[@type='password'][1]" :
                            "//label[text()='" + value.getValue() + "']//following::input[@type='password'][1]"))
                    : (isContainerLabel != null ?
                    "//label//text()[contains(.,'" + value.getValue() + "')]//following::input[@type='password'][1]"
                    : (isFollowingText != null && isFollowingText.equals(true) ?
                    "//label[contains(text(),'" + value.getValue() + "')]//preceding::input[@type='password'][1]" :
                    "//label[contains(text(),'" + value.getValue() + "')]//following::input[@type='password'][1]"));
        } else {
            if (attribute.equals("text")) {
                xpath = operator.equals("equals") ?
                        (isFollowingText != null && isFollowingText.equals(true) ?
                                "//*[text()='" + value.getValue() + "']//preceding::input[@type='password'][1]" :
                                "//*[text()='" + value.getValue() + "']//following::input[@type='password'][1]")
                        :
                        (isFollowingText != null && isFollowingText.equals(true) ?
                                "//*[contains(text(),'" + value.getValue() + "')]//preceding::input[@type='password'][1]" :
                                "//*[contains(text(),'" + value.getValue() + "')]//following::input[@type='password'][1]");
            } else {
                xpath = operator.equals("equals") ?
                        "//input[@type='password' and @" + attribute + "='" + value.getValue() + "']" :
                        "//input[@type='password' and contains(@" + attribute + ",'" + value.getValue() + "')]";
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

