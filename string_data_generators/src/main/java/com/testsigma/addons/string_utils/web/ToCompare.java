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
@Action(actionText = "If test-data-1 operator test-data-2",
        description = "The string relational operators are used to test the relationship between two strings",
        actionType = StepActionType.IF_CONDITION,
        applicationType = ApplicationType.WEB)
public class ToCompare extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Text was Entered in the Expected textarea";
    private static final String ELEMENT_DISABLED = "The Text was Not Entered in the textarea. Element with locator <b>\"%s:%s\"</b> is in disabled state";
    private static final String ERROR_MESSAGE = "Expected textarea Not Found";
    private static final String NOT_UNIQUE_ERROR = "There is more than one occurrence of textarea";
    @TestData(reference = "test-data-1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "operator", allowedValues = {">", "<", ">=", "<=", "=="})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "test-data-2")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public Result execute() throws NoSuchElementException {
        String operatorString = operator.getValue().toString();
        String testDataString1 = testData1.getValue().toString();
        String testDataString2 = testData2.getValue().toString();
        Result returnResult = Result.FAILED;
        if(operatorString.equals(">")) {
            returnResult = testDataString1.compareTo(testDataString2) > 0 ? Result.SUCCESS : Result.FAILED;
        } else if(operatorString.equals("<")){
            returnResult = testDataString1.compareTo(testDataString2) < 0 ? Result.SUCCESS : Result.FAILED;
        } else if(operatorString.equals(">=")) {
            returnResult = testDataString1.equals(testDataString2) || testDataString1.compareTo(testDataString2) > 0 ? Result.SUCCESS : Result.FAILED;
        } else if(operatorString.equals("<=")){
            returnResult = testDataString1.equals(testDataString2) || testDataString1.compareTo(testDataString2) < 0 ? Result.SUCCESS : Result.FAILED;
        } else if(operatorString.equals("==")){
            returnResult = testDataString1.equals(testDataString2) ? Result.SUCCESS : Result.FAILED;
        } else {
            returnResult = Result.FAILED;
        }
        if (returnResult.equals(Result.SUCCESS))
            setSuccessMessage("The TestData satisfies the expected condition");
        else
            setErrorMessage("The TestData does not satisfy the expected condition");
        return returnResult;
    }
}