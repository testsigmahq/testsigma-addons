package com.testsigma.addons.string_utils.android;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "If test-data is operator",
        description = "This action checks if the given string is empty or not",
        actionType = StepActionType.IF_CONDITION,
        applicationType = ApplicationType.ANDROID)
public class CheckIfEmpty extends com.testsigma.addons.string_utils.web.CheckIfEmpty {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "operator", allowedValues = {"empty", "not empty"})
    private com.testsigma.sdk.TestData operator;
    @Override
    public Result execute() throws NoSuchElementException {
        super.setTestData(testData);
        super.setOperator(operator);
        return super.execute();
    }
}

