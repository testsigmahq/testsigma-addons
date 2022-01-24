package com.testsigma.addons.string_utils.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "If test-data operator value",
        description = "This action checks if a given string contains or starts or ends with a provided string",
        actionType = StepActionType.IF_CONDITION,
        applicationType = ApplicationType.WEB)
public class CheckIfContains extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Text was Entered in the Expected textarea";
    private static final String ELEMENT_DISABLED = "The Text was Not Entered in the textarea. Element with locator <b>\"%s:%s\"</b> is in disabled state";
    private static final String ERROR_MESSAGE = "Expected textarea Not Found";
    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence of textarea";
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "operator", allowedValues = {"starts with", "ends with", "contains"})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    @Override
    public Result execute() throws NoSuchElementException {
        String operatorString = operator.getValue().toString();
        String testDataString = testData.getValue().toString();
        String valueString = value.getValue().toString();
        Result returnResult = Result.FAILED;
        switch (operatorString) {
            case "starts with":
                returnResult = testDataString.startsWith(valueString) ? Result.SUCCESS : Result.FAILED;
                break;
            case "ends with":
                returnResult = testDataString.endsWith(valueString) ? Result.SUCCESS : Result.FAILED;
                break;
            case "contains":
                returnResult = testDataString.contains(valueString) ? Result.SUCCESS : Result.FAILED;
                break;
        }
        if (returnResult.equals(Result.SUCCESS))
            setSuccessMessage("The TestData " + operatorString + " the expected value");
        else
            setErrorMessage("The TestData does not " + operatorString + " the expected value");
        return returnResult;
    }
}

