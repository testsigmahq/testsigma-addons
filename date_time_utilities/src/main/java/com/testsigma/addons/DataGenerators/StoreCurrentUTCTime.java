package com.testsigma.addons.DataGenerators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import com.testsigma.addons.util.TimeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;


@Data
@EqualsAndHashCode(callSuper = false)
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Get the current UTC time with format time-format",
        description = "This function returns the current UTC time in the specified format.")
public class StoreCurrentUTCTime extends TestDataFunction {
    @TestDataFunctionParameter(reference = "time-format")
    private com.testsigma.sdk.TestDataParameter timeFormat;

    @Override
    public TestData generate() throws Exception {
        try {
            // Get the current UTC time
            java.time.ZonedDateTime utcTime = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
            String formatStr = timeFormat.getValue().toString();
            
            // Format the result based on requested format
            String formattedResult = TimeUtil.formatTime(utcTime, formatStr);

            logger.info("Generated current UTC time (format: " + formatStr + "): <b>"+ formattedResult +"</b>" );
            
            TestData testData = new TestData(formattedResult);
            return testData;
        } catch (Exception e) {
            String errorMessage = "Failed to generate current UTC time: " + ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            throw new Exception(errorMessage);
        }
    }
}

