package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Get floor or ceil of number and store the result inside a runtimevariable",
        description = "Performs floor or ceil on the given number and stores the result into a runtime variable",
        applicationType = ApplicationType.WEB)
public class FloorAndCeilOfNumber extends WebAction {

    @TestData(reference = "number")
    private com.testsigma.sdk.TestData number;
    @TestData(reference = "operation", allowedValues = {"floor", "ceil"})
    private com.testsigma.sdk.TestData operation;
    @TestData(reference = "runtimevariable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;

        if (number.getValue() == null || number.getValue().toString().isEmpty()) {
            logger.debug("Number is empty, check the source for details");
            setErrorMessage("Operation failed. Please provide a number.");
            return Result.FAILED;
        }

        double inputNumber;
        try {
            inputNumber = Double.parseDouble(number.getValue().toString().trim());
        } catch (NumberFormatException e) {
            logger.debug("Number is not a valid numeric value: " + number.getValue());
            setErrorMessage("Operation failed. '" + number.getValue() + "' is not a valid number.");
            return Result.FAILED;
        }

        String operationString = operation.getValue().toString().trim().toLowerCase();

        runTimeData = new com.testsigma.sdk.RunTimeData();
        long computedValue;

        switch (operationString) {
            case "floor":
                computedValue = (long) Math.floor(inputNumber);
                runTimeData.setValue(String.valueOf(computedValue));
                runTimeData.setKey(runtimeVariable.getValue().toString());
                logger.info("floor of " + inputNumber + " is " + computedValue);
                setSuccessMessage("Successfully performed floor. The result is " + computedValue
                        + " and has been stored into a runtime variable " + runTimeData.getKey());
                break;
            case "ceil":
                computedValue = (long) Math.ceil(inputNumber);
                runTimeData.setValue(String.valueOf(computedValue));
                runTimeData.setKey(runtimeVariable.getValue().toString());
                logger.info("ceil of " + inputNumber + " is " + computedValue);
                setSuccessMessage("Successfully performed ceil. The result is " + computedValue
                        + " and has been stored into a runtime variable " + runTimeData.getKey());
                break;
            default:
                logger.debug("Unsupported operation: " + operationString);
                setErrorMessage("Operation failed. Allowed values are 'floor' and 'ceil'.");
                result = Result.FAILED;
                break;
        }

        return result;
    }
}
