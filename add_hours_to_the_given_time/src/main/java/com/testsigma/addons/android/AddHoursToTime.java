package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@Action(actionText = "Add the hour hours to the time in testdata (HH:mm) and store the result in a runtime variable variable-name",
        description = "This addon takes an input time, adds a specified number of hours to it, and stores the resulting time.",
        applicationType = ApplicationType.ANDROID, useCustomScreenshot = false)

public class AddHoursToTime extends AndroidAction {

    @TestData(reference = "hour")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        // Your Awesome code starts here
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        logger.debug("Time = " + testData2.getValue().toString());
        logger.debug("Hours = " + testData1.getValue().toString());
        logger.debug("Runtime Variable = " + testData.getValue().toString());

        try {

            String inputTime = testData2.getValue().toString();
            int hoursToAdd = Integer.parseInt(testData1.getValue().toString());

            if (hoursToAdd < 1 || hoursToAdd > 24) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Hours should be between 1 and 24");
                return result;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalTime time = LocalTime.parse(inputTime, formatter);

            LocalTime newTime = time.plusHours(hoursToAdd);


            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(newTime.format(formatter));
            runTimeData.setKey(testData.getValue().toString());

            setSuccessMessage("Successfully added " + hoursToAdd + " to the given time " + inputTime
                    + " and stored into a runtime variable " + testData.getValue().toString() + " = "
                    + newTime.format(formatter));

        } catch (DateTimeParseException e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("The given time is not in the expected format (HH:mm). Please provide a valid time.");
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("An error occurred while processing the time. Error: " + ExceptionUtils.getStackTrace(e));
        }
        return result;
    }
}