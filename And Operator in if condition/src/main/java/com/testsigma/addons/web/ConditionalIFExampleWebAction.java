package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "verify if conditions testdata1 operator1 Value1 AND  testdata2 operator2 Value2 are met",
        description = "Conditional If to validate the two condition",
        applicationType = ApplicationType.WEB,
        actionType = StepActionType.IF_CONDITION,
        useCustomScreenshot = false)

public class ConditionalIFExampleWebAction extends WebAction {

    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "Value1")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "Value2")
    private com.testsigma.sdk.TestData testData4;

    @TestData(reference = "operator1", allowedValues = {"==", "!=", ">", "<", ">=", "<="})
    private com.testsigma.sdk.TestData firstOperator;
    @TestData(reference = "operator2", allowedValues = {"==", "!=", ">", "<", ">=", "<="})
    private com.testsigma.sdk.TestData secondOperator;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        logger.debug("testdata1 : " + testData1.getValue().toString());
        logger.debug("value1 : " + testData2.getValue().toString());
        logger.debug("testdata2 : " + testData3.getValue().toString());
        logger.debug("value2 : " + testData4.getValue().toString());

        String data1 = testData1.getValue().toString();
        String value1 = testData2.getValue().toString();
        String data2 = testData3.getValue().toString();
        String value2 = testData4.getValue().toString();

        String comparator1 = firstOperator.getValue().toString();
        String comparator2 = secondOperator.getValue().toString();


        // Evaluate conditions using the extracted method
        boolean condition1 = evaluateCondition(data1, value1, comparator1);
        boolean condition2 = evaluateCondition(data2, value2, comparator2);

        if (condition1 && condition2) {
            setSuccessMessage("Successfully verified both the conditions");
        } else {
                if (!condition1 && !condition2) {
+                setErrorMessage("Both conditions failed");
+            } else if (!condition1) {
                setErrorMessage("The first condition failed");
            } else if (!condition2) {
                setErrorMessage("The second condition failed");
            }
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private boolean evaluateCondition(String data, String value, String comparator) {
        switch (comparator) {
            case "==":
                return data.equals(value);
            case "!=":
                return !data.equals(value);
            case ">":
                return data.compareTo(value) > 0;
            case "<":
                return data.compareTo(value) < 0;
            case ">=":
                return data.compareTo(value) >= 0;
            case "<=":
                return data.compareTo(value) <= 0;
            default:
                throw new IllegalArgumentException("Invalid comparator: " + comparator);
        }
    }
}
