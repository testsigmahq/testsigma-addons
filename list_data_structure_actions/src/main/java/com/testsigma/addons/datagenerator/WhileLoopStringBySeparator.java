package com.testsigma.addons.datagenerator;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.RunTimeDataProvider;
import com.testsigma.sdk.annotation.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@TestDataFunction(displayName = "Store current iteration value using separator separated values",
        description = "Iterate separator separated values in test-data and return the current iteration value")
public class WhileLoopStringBySeparator extends com.testsigma.sdk.TestDataFunction {

    @TestDataFunctionParameter(reference = "separator")
    private com.testsigma.sdk.TestData separator;

    @TestDataFunctionParameter(reference = "test-data")
    private com.testsigma.sdk.TestData inputData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData currentIterationIndex;

    @RunTimeDataProvider
    private com.testsigma.sdk.RuntimeDataProvider currentIterationIndexProvider;

    @Override
    public TestData generate() throws Exception {
        try {
            String separatorValue = this.separator.getValue().toString();
            String inputValue = this.inputData.getValue().toString();
            String[] inputValues = inputValue.split(separatorValue);

            String indexKey = "WhileLoopStringBySeparator_TSIndex";

            Object currentIterationIndexData = null;
            try {
                currentIterationIndexData = currentIterationIndexProvider.getRuntimeData(indexKey);
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

            if (inputValues.length <= currentIterationIndexValue) {
                logger.info("No more values to iterate the while loop.");
                setErrorMessage("No more values to iterate the while loop.");
                throw new Exception("No more values to iterate. All values have been consumed.");
            }

            String iterationValue = inputValues[currentIterationIndexValue].trim();

            currentIterationIndexValue++;
            currentIterationIndex = new com.testsigma.sdk.RunTimeData();
            currentIterationIndex.setKey(indexKey);
            currentIterationIndex.setValue(String.valueOf(currentIterationIndexValue));
            logger.info("Stored next index " + indexKey + " = " + currentIterationIndexValue);

            setSuccessMessage(String.format("Successfully returned iteration value: <b>%s</b>.", iterationValue));
            logger.info("Returning iteration value: " + iterationValue);
            return new TestData(iterationValue);
        } catch (Exception e) {
            setErrorMessage("Failed to iterate the while loop due to " + e.getMessage());
            logger.warn("Failed to iterate the while loop." + ExceptionUtils.getStackTrace(e));
            throw new Exception("Failed to iterate the while loop due to " + e.getMessage());
        }
    }

}
