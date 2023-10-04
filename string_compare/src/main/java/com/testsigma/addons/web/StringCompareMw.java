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
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "string2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "selectable-list", allowedValues = {"equals","equals ignore-case","contains","contains ignore-case"})
    private com.testsigma.sdk.TestData testData3;


    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        logger.debug(" test-data1: "+ this.testData1.getValue()+"test data2:"+this.testData2.getValue()+"operation:"+this.testData3.getValue());
        com.testsigma.sdk.Result result;

        String str1=String.valueOf(testData1.getValue());
        String str2=String.valueOf(testData2.getValue());
        String operation = String.valueOf(testData3.getValue());


        StringCompareUtil util = new StringCompareUtil();
        boolean operationResult = util.performOperation(str1,str2,operation);

        if (operationResult) {
            logger.info(util.getSuccessMessage());
            setSuccessMessage(util.getSuccessMessage());
            System.out.println(util.getSuccessMessage());
            result = com.testsigma.sdk.Result.SUCCESS;
        } else {
            logger.info("Operation failed: " + util.getErrorMessage());
            setErrorMessage(util.getErrorMessage());
            System.out.println(util.getErrorMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
