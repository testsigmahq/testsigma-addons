package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "count of elements identified by count-element is selectable-list test data",
        description = "Conditional If example for value comparison",
        applicationType = ApplicationType.WEB,
        actionType = StepActionType.IF_CONDITION)
public class ConditionalIFExampleWebAction extends WebAction {

    @Element(reference = "count-element")
    private com.testsigma.sdk.Element element;
    @TestData(reference = "test data")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "selectable-list", allowedValues = {"equal to", "greater than or equal to", "less than or equal to"})
    private com.testsigma.sdk.TestData testData2;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.debug("element locator with : " + this.element.getValue() + " by:" + this.element.getBy() + ", test-data: " + this.testData1.getValue());
        WebElement webElement = element.getElement();
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        switch (testData2.getValue().toString()) {
            case "less than or equal to":
                if (testData1.getValue().toString().compareTo(webElement.getText()) <= 0) {
                    setSuccessMessage("Successfully executed step");
                } else {
                    return Result.FAILED;
                }
                break;
            case "greater than or equal to":
                if (testData1.getValue().toString().compareTo(webElement.getText()) >= 0) {
                    setSuccessMessage("Successfully executed step");
                } else {
                    return Result.FAILED;
                }
                break;
            case "equal to":
                if (testData1.getValue().toString().compareTo(webElement.getText()) == 0) {
                    setSuccessMessage("Successfully executed step");
                } else {
                    return Result.FAILED;
                }
                break;
        }
        return result;
    }
}