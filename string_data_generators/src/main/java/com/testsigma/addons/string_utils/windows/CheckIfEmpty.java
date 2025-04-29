package com.testsigma.addons.string_utils.windows;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "If test-data is operator",
        description = "This action checks if a given string contains or starts or ends with a provided string",
        actionType = StepActionType.IF_CONDITION,
        applicationType = ApplicationType.WINDOWS)
public class CheckIfEmpty extends WindowsAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "operator", allowedValues = {"empty", "not empty"})
    private com.testsigma.sdk.TestData operator;

    @Override
    public Result execute() throws NoSuchElementException {
        String operatorString = operator.getValue().toString();
        String testDataString = testData.getValue() != null ? testData.getValue().toString().trim() : null;
        Result returnResult;
        if (operatorString.equals("empty"))
            returnResult = testDataString != null && testDataString.isEmpty() ? Result.SUCCESS : Result.FAILED;
        else
            returnResult = testDataString != null && testDataString.isEmpty() ? Result.FAILED : Result.SUCCESS;
        return returnResult;
    }
}

