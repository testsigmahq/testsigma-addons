package com.testsigma.addons.DataGenerators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import com.testsigma.addons.util.TimeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Add time-amount time-unit to the input time test-data and return result in format time-format",
        description = "Add specified time duration to the input time and return the result in the format specified by time-format")
public class AddTimeToGivenTime extends TestDataFunction {
    @TestDataFunctionParameter(reference = "test-data")
    private com.testsigma.sdk.TestDataParameter testData;
    @TestDataFunctionParameter(reference = "time-amount")
    private com.testsigma.sdk.TestDataParameter timeAmount;
    @TestDataFunctionParameter(reference = "time-unit")
    private com.testsigma.sdk.TestDataParameter timeUnit;
    @TestDataFunctionParameter(reference = "time-format")
    private com.testsigma.sdk.TestDataParameter timeFormat;

    @Override
    public TestData generate() throws Exception {
        try{
            logger.info("Starting execution of AddTimeToGivenTime data generator");
            String inputTimeStr = testData.getValue().toString();
            long amount = Long.parseLong(timeAmount.getValue().toString());
            String unitStr = timeUnit.getValue().toString();
            String outputFormatStr = timeFormat.getValue().toString();
            
            // Auto-detect input format
            TimeUtil.DetectedFormat detectedInput = TimeUtil.detectInputFormat(inputTimeStr);
            String detectedInputFormatStr = detectedInput.formatStr;
            TimeUtil.FormatType inputType = detectedInput.type;
            TimeUtil.FormatType outputType = TimeUtil.getFormatType(outputFormatStr);
            
            // Validate conversion compatibility
            TimeUtil.validateConversion(inputType, outputType);
            
            // Parse the input time based on the detected format
            LocalDateTime inputTime = TimeUtil.parseTime(inputTimeStr);
            
            // Add time based on unit
            LocalDateTime resultTime;
            switch(unitStr) {
                case "SECONDS":
                    resultTime = inputTime.plusSeconds(amount);
                    break;
                case "MINUTES":
                    resultTime = inputTime.plusMinutes(amount);
                    break;
                case "HOURS":
                    resultTime = inputTime.plusHours(amount);
                    break;
                case "DAYS":
                    resultTime = inputTime.plusDays(amount);
                    break;
                case "WEEKS":
                    resultTime = inputTime.plusWeeks(amount);
                    break;
                case "MONTHS":
                    resultTime = inputTime.plusMonths(amount);
                    break;
                case "YEARS":
                    resultTime = inputTime.plusYears(amount);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid time unit: " + unitStr);
            }
            
            // Format the result based on requested output format
            String formattedResult = TimeUtil.formatTime(resultTime, outputFormatStr);
            
            logger.info("Added " + amount + " " + unitStr + " to time: <b>" + inputTimeStr + "</b> (detected format: " + detectedInputFormatStr + ", output format: " + outputFormatStr + "). Result: <b>" + formattedResult + "</b>");
            
            TestData testData = new TestData(formattedResult);
            return testData;
        } catch (IllegalArgumentException e) {
            // Handle conversion errors specifically
            String errorMessage = e.getMessage();
            logger.warn(errorMessage);
            throw new Exception("Failed to add time to given time: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = "Failed to add time to given time: " + ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            throw new Exception(errorMessage);
        }
    }
}

