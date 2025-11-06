package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.addons.util.TimeUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Add time-amount time-unit to the input time test-data and store result in format time-format into a variable runtime-variable",
        description = "Add specified time duration to the input time and store the result in the runtime variable in the format specified by time-format",
        applicationType = com.testsigma.sdk.ApplicationType.WEB)
public class AddTimeToGivenTime extends WebAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "time-amount")
    private com.testsigma.sdk.TestData timeAmount;
    @TestData(reference = "time-unit",allowedValues = {"SECONDS","MINUTES","HOURS","DAYS","WEEKS","MONTHS","YEARS"})
    private com.testsigma.sdk.TestData timeUnit;
    @TestData(reference = "time-format", allowedValues = {
            "HH:MM", "HH:MM:SS",
            "YYYY-MM-DD", "DD-MM-YYYY", "MM/DD/YYYY", "DD/MM/YYYY",
            "YYYY-MM-DD HH:MM", "YYYY-MM-DD HH:MM:SS",
            "DD-MM-YYYY HH:MM", "DD-MM-YYYY HH:MM:SS",
            "MM/DD/YYYY HH:MM", "MM/DD/YYYY HH:MM:SS",
            "DD/MM/YYYY HH:MM", "DD/MM/YYYY HH:MM:SS",
            "hh:mm AM/PM", "hh:mm:ss AM/PM",
            "YYYY-MM-DD hh:mm AM/PM", "YYYY-MM-DD hh:mm:ss AM/PM",
            "YYYY-MM-DDTHH:MM:SS", "YYYY-MM-DDTHH:MM:SSZ"
    })
    private com.testsigma.sdk.TestData timeFormat;

    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        // Implementation goes here
        try{
            logger.info("Starting execution of AddTimeToGivenTime action");
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
            
            // Store the result in runtime variable
            String runtimeVar = runtimeVariable.getValue().toString();
            runTimeData.setKey(runtimeVar);
            runTimeData.setValue(formattedResult);
            logger.info("Added " + amount + " " + unitStr + " to time: <b>" + inputTimeStr + "</b> (detected format: " + detectedInputFormatStr + ", output format: " + outputFormatStr + "). Result stored in <b>" + runtimeVar + "=" + formattedResult + "</b>");
            
            setSuccessMessage("Successfully added time to given time: <b>" + runtimeVar + "=" + formattedResult +
                    "</b>, input time was " + inputTimeStr + " (detected format: " + detectedInputFormatStr + ", output format: " + outputFormatStr + ")");
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (IllegalArgumentException e) {
            // Handle conversion errors specifically
            String errorMessage = e.getMessage();
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
            return com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            String errorMessage = "Failed to add time to given time: " + ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
            return com.testsigma.sdk.Result.FAILED;
        }
    }



}
