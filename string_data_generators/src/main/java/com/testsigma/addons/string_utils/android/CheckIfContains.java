package com.testsigma.addons.string_utils.android;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "If test-data operator value",
        description = "This action checks if a given string contains or starts or ends with a provided string",
        actionType = StepActionType.IF_CONDITION,
        applicationType = ApplicationType.ANDROID)
public class CheckIfContains extends com.testsigma.addons.string_utils.web.CheckIfContains {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "operator", allowedValues = {"starts with", "ends with", "contains"})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
    
}