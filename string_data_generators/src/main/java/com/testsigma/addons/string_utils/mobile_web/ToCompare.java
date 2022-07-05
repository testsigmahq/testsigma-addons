package com.testsigma.addons.string_utils.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "If test-data-1 operator test-data-2",
        description = "The string relational operators are used to test the relationship between two strings",
        actionType = StepActionType.IF_CONDITION,
        applicationType = ApplicationType.MOBILE_WEB)
public class ToCompare extends com.testsigma.addons.string_utils.web.ToCompare {
    @TestData(reference = "test-data-1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "operator", allowedValues = {">", "<", ">=", "<=", "=="})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "test-data-2")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setTestData1(testData1);
        super.setOperator(operator);
        super.setTestData2(testData2);
        return super.execute();
    }
}
