package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if string1 selectable-list with string2",
        description = "Verify if both the string equals/contains with and without ignore-case",
        applicationType = ApplicationType.MOBILE_WEB)
public class StringCompareMw extends WebAction {

    @TestData(reference = "string1")
    private com.testsigma.sdk.TestData Actual_Value;
    @TestData(reference = "string2")
    private com.testsigma.sdk.TestData Expected_Value;
    @TestData(reference = "selectable-list", allowedValues = {"equals","equals ignore-case","contains","contains ignore-case"})
    private com.testsigma.sdk.TestData Compared_Value;


    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        logger.info(" ActualValue: "+ this.Actual_Value.getValue() +"  "+ " ExpectedValue: "+this.Expected_Value.getValue()+"  "+" Operation: "+this.Compared_Value.getValue());
        com.testsigma.sdk.Result result;

        String str1=String.valueOf(Actual_Value.getValue());
        String str2=String.valueOf(Expected_Value.getValue());
        String operation = String.valueOf(Compared_Value.getValue());


        StringCompareUtil util = new StringCompareUtil();
        boolean operationResult = util.performOperation(str1,str2,operation);

        if (operationResult) {
            //logger.info(getSuccessMessage());
            setSuccessMessage(getSuccessMessage());
            result = com.testsigma.sdk.Result.SUCCESS;
        } else {
            //logger.info("Operation failed: " + getErrorMessage());
            setErrorMessage(getErrorMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
