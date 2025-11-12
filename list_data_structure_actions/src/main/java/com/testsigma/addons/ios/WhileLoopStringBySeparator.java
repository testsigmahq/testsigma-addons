package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.RunTimeDataProvider;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "store the current iteration value using SEPARATOR separated values in TEST-DATA " +
        "into a runtime variable RUNTIME-VARIABLE",
        description = "Iterate SEPARATOR separated values in TEST-DATA and store current iteration value " +
                "in RUNTIME-VARIABLE",
        applicationType = ApplicationType.WEB,
        actionType = StepActionType.WHILE_LOOP)
public class WhileLoopStringBySeparator extends WebAction {

    @TestData(reference = "SEPARATOR")
    private com.testsigma.sdk.TestData separator;

    @TestData(reference = "TEST-DATA")
    private com.testsigma.sdk.TestData inputData;

    @TestData(reference = "RUNTIME-VARIABLE", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    // Reference to runtime-variable key
    @RunTimeData
    private com.testsigma.sdk.RunTimeData currentIteration;

    // Reference to runtime-variable key + "some_random_string"
    @RunTimeData
    private com.testsigma.sdk.RunTimeData currentIterationIndex;

    // To fetch currentIterationIndex value
    @RunTimeDataProvider
    private com.testsigma.sdk.RuntimeDataProvider currentIterationIndexProvider;

    @Override
    protected Result execute() throws NoSuchElementException {
        try {
            String runtimeVariableName = runtimeVariable.getValue().toString();
            String indexedRuntimeVariableName = runtimeVariableName + "_TSIndex"; // variable to hold index value

            String separatorValue = this.separator.getValue().toString();
            String inputValue = this.inputData.getValue().toString();
            String[] inputValues = inputValue.split(separatorValue);

            // Get the current iteration index
            Object currentIterationIndexData = null;
            try {
                currentIterationIndexData = currentIterationIndexProvider.getRuntimeData(indexedRuntimeVariableName);
            } catch (Exception ex) {
               logger.info("Failed to get currentIterationIndexData " + ex.getMessage());
            }

            int currentIterationIndexValue;
            if (currentIterationIndexData != null) {
                currentIterationIndexValue = Integer.parseInt(currentIterationIndexData.toString());
            } else {
                logger.info("Current iteration index is null");
                currentIterationIndexValue = 0;
            }
            logger.info("Current iteration index value: " + currentIterationIndexValue);

            // Check if index is out of bound
            if (inputValues.length <= currentIterationIndexValue) {
                logger.info("No more values to iterate the while loop.");
                setErrorMessage("No more values to iterate the while loop.");
                return Result.FAILED;
            }

            // Get the current iteration value
            String iterationValue = inputValues[currentIterationIndexValue].trim();

            // Store the current iteration value
            currentIteration = new com.testsigma.sdk.RunTimeData();
            currentIteration.setKey(runtimeVariableName);
            currentIteration.setValue(iterationValue);
            logger.info("Stored " + runtimeVariableName + " = " + iterationValue);

            // Increment the index and store
            currentIterationIndexValue++;
            currentIterationIndex = new com.testsigma.sdk.RunTimeData();
            currentIterationIndex.setKey(indexedRuntimeVariableName);
            currentIterationIndex.setValue(String.valueOf(currentIterationIndexValue));
            logger.info("Stored next index" + indexedRuntimeVariableName + " = " + currentIterationIndexValue);
            setSuccessMessage(String.format("Successfully stored the current iteration value into the " +
                    "runtime variable <b>%s = %s </b>.", runtimeVariableName, iterationValue));
            // Return success
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Failed to iterate the while loop." + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to iterate the while loop. " + e.getMessage());
            return Result.FAILED;
        }
    }

}
