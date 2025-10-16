package com.testsigma.addons.WindowLite;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.Test;

@Data
@Action(actionText = "Verify if test-data1 operator test-data2",
        description = "This action verifies if the two strings match based on the specified operator",
        applicationType = ApplicationType.WINDOWS,
        actionType = StepActionType.IF_CONDITION,
        displayName = "Verify if strings match the operator",
        useCustomScreenshot = false)
public class VerifyTestDataInIfcondition extends WindowsAction {

    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "operator",
            allowedValues = {"==", ">", "<=", ">=", "!=", "less than", "contains"})
    private com.testsigma.sdk.TestData operator;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Verify If Strings Match The Operator: Starting Execution ===");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        if(testData1.getValue() == null || testData2.getValue() == null || operator.getValue() == null) {
            setErrorMessage("One or more input values are null");
            return com.testsigma.sdk.Result.FAILED;
        }
        String str1 = testData1.getValue().toString();
        String str2 = testData2.getValue().toString();
        String op = operator.getValue().toString();

        // try int comparison first
        try {
            int num1 = Integer.parseInt(str1);
            int num2 = Integer.parseInt(str2);
            boolean comparisonResult = compareNumbers(num1, num2, op);

        } catch (NumberFormatException e) {
            // If parsing fails, fall back to string comparison
            boolean comparisonResult = compareStrings(str1, str2, op);
            if (!comparisonResult) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("String comparison failed: " + str1 + " " + op + " " + str2);
            } else {
                setSuccessMessage("String comparison succeeded: " + str1 + " " + op + " " + str2);
            }
        }
        return null;
    }
    private boolean compareNumbers(int num1, int num2, String op) {
        switch (op) {
            case "==":
                return num1 == num2;
            case "<":
            case "less than":
                return num1 < num2;
            case ">":
                return num1 > num2;
            case "<=":
                return num1 <= num2;
            case ">=":
                return num1 >= num2;
            case "!=":
                return num1 != num2;
            case "contains":
                // For numbers, convert to string and check if one contains the other
                return String.valueOf(num1).contains(String.valueOf(num2));
            default:
                setErrorMessage("Invalid operator: " + op);
                return false;
        }
    }
    private boolean compareStrings(String str1, String str2, String op) {
        switch (op) {
            case "==":
                return str1.equals(str2);
            case "<":
            case "less than":
                return str1.compareTo(str2) < 0;
            case ">":
                return str1.compareTo(str2) > 0;
            case "<=":
                return str1.compareTo(str2) <= 0;
            case ">=":
                return str1.compareTo(str2) >= 0;
            case "!=":
                return !str1.equals(str2);
            case "contains":
                return str1.contains(str2);
            default:
                setErrorMessage("Invalid operator: " + op);
                return false;
        }
    }

}
