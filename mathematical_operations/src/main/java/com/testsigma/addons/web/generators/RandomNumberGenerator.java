package com.testsigma.addons.web.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(
        displayName = "Generate random number within range min and max with decimal_places",
        description = "Generates a random number within the given range, optionally with decimal places. Use this with mathematical operations addon.")
public class RandomNumberGenerator extends TestDataFunction {

    @TestDataFunctionParameter(reference = "min")
    private com.testsigma.sdk.TestDataParameter min;
    @TestDataFunctionParameter(reference = "max")
    private com.testsigma.sdk.TestDataParameter max;
    @TestDataFunctionParameter(reference = "decimal_places")
    private com.testsigma.sdk.TestDataParameter decimalPlaces;

    @Override
    public TestData generate() throws Exception {
        logger.info("Initiating random number generation");
        double minVal = Double.parseDouble(min.getValue().toString().replaceAll("[$,@,%,#]", "").trim());
        double maxVal = Double.parseDouble(max.getValue().toString().replaceAll("[$,@,%,#]", "").trim());
        int decimals = 0;
        if (decimalPlaces != null && decimalPlaces.getValue() != null && !decimalPlaces.getValue().toString().trim().isEmpty()) {
            decimals = Integer.parseInt(decimalPlaces.getValue().toString().trim());
        }
        double number = minVal + (Math.random() * (maxVal - minVal));
        String formatted = decimals > 0 ? String.format("%." + decimals + "f", number) : String.valueOf((int) number);
        TestData testData = new TestData(formatted);
        logger.info("Generated random number: " + formatted);
        return testData;
    }
}
