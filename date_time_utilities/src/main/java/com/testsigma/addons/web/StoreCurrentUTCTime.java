package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.testsigma.addons.util.TimeUtil;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Store the current time with format time-format into a variable runtime-variable",
        description = "This action stores the current UTC time into a specified runtime variable.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB)
public class StoreCurrentUTCTime extends WebAction {
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    @TestData(reference = "time-format", allowedValues = {
            "UTC",
            "ISO_8601",
            "HH:MM", "HH:MM:SS","hh:mm AM/PM", "hh:mm:ss AM/PM",
            "YYYY-MM-DD", "DD-MM-YYYY", "MM/DD/YYYY", "DD/MM/YYYY",
            "YYYY-MM-DD HH:MM", "YYYY-MM-DD HH:MM:SS",
            "DD-MM-YYYY HH:MM", "DD-MM-YYYY HH:MM:SS",
            "MM/DD/YYYY HH:MM", "MM/DD/YYYY HH:MM:SS",
            "DD/MM/YYYY HH:MM", "DD/MM/YYYY HH:MM:SS",
            "YYYY-MM-DD hh:mm AM/PM", "YYYY-MM-DD hh:mm:ss AM/PM",
            "YYYY-MM-DDTHH:MM:SS", "YYYY-MM-DDTHH:MM:SSZ"
    })
    private com.testsigma.sdk.TestData timeFormat;

    @Override
    public com.testsigma.sdk.Result execute() {
        try {
            // Get the current UTC time
            java.time.ZonedDateTime utcTime = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
            String formatStr = timeFormat.getValue().toString();
            
            String formattedResult = TimeUtil.formatTime(utcTime, formatStr);

            String runtimeVar = runtimeVariable.getValue().toString();
            runTimeData.setKey(runtimeVar);
            runTimeData.setValue(formattedResult);
            logger.info("Stored current time (format: " + formatStr + "): <b>"+ runtimeVar + "=" + formattedResult +"</b>" );
            setSuccessMessage("Successfully stored current time in runtime variable: " + runtimeVar + "=" + formattedResult);
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "Failed to store current time: " + ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
