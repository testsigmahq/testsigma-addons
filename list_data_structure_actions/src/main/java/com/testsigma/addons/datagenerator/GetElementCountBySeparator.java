package com.testsigma.addons.datagenerator;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@TestDataFunction(displayName = "Get count of elements in separator separated test-data",
        description = "Splits the test-data using the given separator and returns the count of elements")
public class GetElementCountBySeparator extends com.testsigma.sdk.TestDataFunction {

    @TestDataFunctionParameter(reference = "separator")
    private com.testsigma.sdk.TestData separator;

    @TestDataFunctionParameter(reference = "test-data")
    private com.testsigma.sdk.TestData inputData;

    @Override
    public TestData generate() throws Exception {
        try {
            String separatorValue = this.separator.getValue().toString();
            String inputValue = this.inputData.getValue().toString();

            if (inputValue.isEmpty()) {
                logger.info("Input data is empty, returning count as 0");
                setSuccessMessage("Input data is empty. Element count: <b>0</b>.");
                return new TestData("0");
            }

            String[] elements = inputValue.split(separatorValue, -1);
            int count = elements.length;

            logger.info("Element count: " + count);
            setSuccessMessage(String.format("Successfully retrieved element count: <b>%d</b>.", count));
            return new TestData(String.valueOf(count));
        } catch (Exception e) {
            setErrorMessage("Failed to get element count due to " + e.getMessage());
            logger.warn("Failed to get element count." + ExceptionUtils.getStackTrace(e));
            throw new Exception("Failed to get element count due to " + e.getMessage());
        }
    }

}
