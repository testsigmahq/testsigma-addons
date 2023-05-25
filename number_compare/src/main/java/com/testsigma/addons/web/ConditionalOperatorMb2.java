package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if test-data-1 operator test-data-2",
        description = "This is a numeric operation that finds relation between two numeric value",
        applicationType = ApplicationType.MOBILE_WEB)
public class ConditionalOperatorMb2 extends ConditionalOperatorsWeb {

   
    @TestData(reference = "test-data-1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "operator", allowedValues = {">", "<", ">=", "<=", "=="})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "test-data-2")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public Result execute() throws NoSuchElementException {
       super.setTestData1(this.testData1);
       super.setTestData2(this.testData2);
       super.setOperator(this.operator);
           
        return super.execute();
    }
}
