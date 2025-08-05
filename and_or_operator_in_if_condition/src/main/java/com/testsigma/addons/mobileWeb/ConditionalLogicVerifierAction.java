package com.testsigma.addons.mobileWeb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if conditions testdata1 operator1 Value1 operator3  testdata2 operator2 Value2 are met",
        description = "Conditional If to validate the two condition for numbers and strings",
        applicationType = ApplicationType.MOBILE_WEB,
        actionType = StepActionType.IF_CONDITION,
        useCustomScreenshot = false)

public class ConditionalLogicVerifierAction extends WebAction {

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
    @TestData(reference = "operator3", allowedValues = {"AND", "OR"})
    private com.testsigma.sdk.TestData thirdOperator;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        // Log input values
        logger.debug("testdata1 : " + testData1.getValue().toString());
        logger.debug("value1 : " + testData3.getValue().toString());
        logger.debug("testdata2 : " + testData2.getValue().toString());
        logger.debug("value2 : " + testData4.getValue().toString());

        // Extract values
        String data1 = testData1.getValue().toString();
        String value1 = testData3.getValue().toString();
        String data2 = testData2.getValue().toString();
        String value2 = testData4.getValue().toString();

        String comparator1 = firstOperator.getValue().toString();
        String comparator2 = secondOperator.getValue().toString();
        String comparator3 = thirdOperator.getValue().toString();

        // Log operators for debugging
        logger.debug("Operator1 (for condition1): " + comparator1);
        logger.debug("Operator2 (for condition2): " + comparator2);
        logger.debug("Operator3 (logical): " + comparator3);

        logger.debug("Condition1: '" + data1 + "' " + comparator1 + " '" + value1 + "'");
        logger.debug("Condition2: '" + data2 + "' " + comparator2 + " '" + value2 + "'");

        // Evaluate conditions using the enhanced method
        boolean condition1 = evaluateCondition(data1, value1, comparator1);
        boolean condition2 = evaluateCondition(data2, value2, comparator2);

        // Log condition results
        logger.debug("Condition1 result: " + condition1);
        logger.debug("Condition2 result: " + condition2);

        // Apply logical operator
        if ("AND".equalsIgnoreCase(comparator3)) {
            if (condition1 && condition2) {
                setSuccessMessage("Successfully verified that both the conditions are met");
                logger.info("Both conditions passed with AND operator");
            } else {
                if (!condition1 && !condition2) {
                    setErrorMessage("Both conditions failed");
                    logger.debug("Both condition1 and condition2 are false");
                } else if (!condition1) {
                    setErrorMessage("The first condition failed");
                    logger.debug("Only condition1 is false");
                } else if (!condition2) {
                    setErrorMessage("The second condition failed");
                    logger.debug("Only condition2 is false");
                }
                result = com.testsigma.sdk.Result.FAILED;
            }
        } else if ("OR".equalsIgnoreCase(comparator3)) {
            if (condition1 || condition2) {
                setSuccessMessage("Successfully verified at least one condition is met");
                logger.info("At least one condition passed with OR operator");
            } else {
                setErrorMessage("Both conditions failed");
                logger.debug("Both conditions failed with OR operator");
                result = com.testsigma.sdk.Result.FAILED;
            }
        }

        return result;
    }

    private boolean evaluateCondition(String data, String value, String comparator) {
        boolean result;

        switch (comparator) {
            case "==":
                // For equality, handle both numeric and string cases
                if (isNumeric(data) && isNumeric(value)) {
                    result = Double.parseDouble(data.trim()) == Double.parseDouble(value.trim());
                    logger.debug("Numeric equality check: " + data + " == " + value + " = " + result);
                } else {
                    result = data.equals(value);
                    logger.debug("String equality check: '" + data + "' == '" + value + "' = " + result);
                }
                break;

            case "!=":
                // For inequality, handle both numeric and string cases
                if (isNumeric(data) && isNumeric(value)) {
                    result = Double.parseDouble(data.trim()) != Double.parseDouble(value.trim());
                    logger.debug("Numeric inequality check: " + data + " != " + value + " = " + result);
                } else {
                    result = !data.equals(value);
                    logger.debug("String inequality check: '" + data + "' != '" + value + "' = " + result);
                }
                break;

            case ">":
                result = compareValues(data, value) > 0;
                break;

            case "<":
                result = compareValues(data, value) < 0;
                break;

            case ">=":
                result = compareValues(data, value) >= 0;
                break;

            case "<=":
                result = compareValues(data, value) <= 0;
                break;

            default:
                throw new IllegalArgumentException("Invalid comparator: " + comparator);
        }

        logger.debug("evaluateCondition('" + data + "', '" + value + "', '" + comparator + "') = " + result);
        return result;
    }

    private int compareValues(String data, String value) {
        // Check if both values are numeric
        if (isNumeric(data) && isNumeric(value)) {
            // Numeric comparison
            Double dataNum = Double.parseDouble(data.trim());
            Double valueNum = Double.parseDouble(value.trim());
            int result = dataNum.compareTo(valueNum);
            logger.debug("Numeric comparison: " + dataNum + " vs " + valueNum + " = " + result);
            return result;
        } else {
            // String (lexicographic) comparison
            int result = data.compareTo(value);
            logger.debug("String comparison: '" + data + "' vs '" + value + "' = " + result);
            return result;
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}