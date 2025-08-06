package com.testsigma.addons.RestApi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "calculate time difference in unit between timestamp testdata1 and timestamp testdata2 " +
        "and store the result in variable runtime-variable",
        description = "calculate difference between two times",
        applicationType = ApplicationType.REST_API,
        useCustomScreenshot = false)
public class GetTimeDifferenceInMinutes extends RestApiAction {

    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "unit", allowedValues = {"HOURS", "MINUTES", "SECONDS", "HH:MM:SS", "HH:MM"})
    private com.testsigma.sdk.TestData timeUnit;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        logger.info("testData1: " + testData1);
        logger.info("testData2: " + testData2);

        try {
            String time1Str = testData1.getValue().toString().trim();
            String time2Str = testData2.getValue().toString().trim();
            String unit = timeUnit.getValue().toString().toUpperCase();

            Date date1 = parseTimeString(time1Str);
            Date date2 = parseTimeString(time2Str);

            if (date1 == null || date2 == null) {
                logger.debug("Failed to parse one or both timestamps");
                return Result.FAILED;
            }

            long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
            String formattedResult = null;

            switch (unit) {
                case "HOURS":
                    long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                    formattedResult = String.valueOf(hours);
                    break;
                case "MINUTES":
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
                    formattedResult = String.valueOf(minutes);
                    break;
                case "SECONDS":
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
                    formattedResult = String.valueOf(seconds);
                    break;
                case "HH:MM:SS":
                    long totalHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                    long remainingMinutes = (TimeUnit.MILLISECONDS.toMinutes(diffInMillis) - (totalHours * 60)) % 60;
                    long remainingSeconds = (TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
                            - totalHours * 60 - remainingMinutes * 60) % 60;
                    formattedResult = String.format("%02d:%02d:%02d", totalHours, remainingMinutes, remainingSeconds);
                    break;
                case "HH:MM":
                    long totalHoursHM = TimeUnit.MILLISECONDS.toHours(diffInMillis);
                    long remainingMinutesHM = (TimeUnit.MILLISECONDS.toMinutes(diffInMillis) - (totalHoursHM * 60)) % 60;
                    formattedResult = String.format("%02d:%02d", totalHoursHM, remainingMinutesHM);
                    break;
            }

            logger.info("Time difference in " + formattedResult);
            runTimeData.setValue(String.valueOf(formattedResult));
            runTimeData.setKey(runtimeVariable.getValue().toString());
            logger.debug("runTimeData: " + runTimeData.getValue());
            setSuccessMessage("Successfully stored the time difference (format: "+ unit +") in runtime variable <b>" +
                    runTimeData.getKey() + " = " + runTimeData.getValue() + "</b>");

        } catch (Exception e) {
            logger.debug("Error calculating time difference " + e);
            setErrorMessage("Failed to calculate time difference: " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }

    private Date parseTimeString(String timeString) {
        try {
            // Remove any text in parentheses or after //
            timeString = timeString.replaceAll("\\(.*\\)", "").trim();
            String[] parts = timeString.split("//");
            if (parts.length > 1) {
                timeString = parts[0].trim();
            }

            // Current date to set the time on
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = dateFormat.format(today);

            // Check for time with AM/PM
            Pattern timePattern12Hr = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))? ?(AM|PM)",
                    Pattern.CASE_INSENSITIVE);
            Matcher timeMatcher12Hr = timePattern12Hr.matcher(timeString);
            if (timeMatcher12Hr.find()) {
                String hour = timeMatcher12Hr.group(1);
                String minute = timeMatcher12Hr.group(2);
                String second = timeMatcher12Hr.group(3) != null ? timeMatcher12Hr.group(3) : "00";
                String ampm = timeMatcher12Hr.group(4).toUpperCase();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                return sdf.parse(todayStr + " " + hour + ":" + minute + ":" + second + " " + ampm);
            }

            // Check for 24-hour format
            Pattern timePattern24Hr = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
            Matcher timeMatcher24Hr = timePattern24Hr.matcher(timeString);
            if (timeMatcher24Hr.find() && !timeString.toLowerCase().contains("am") &&
                    !timeString.toLowerCase().contains("pm")) {
                String hour = timeMatcher24Hr.group(1);
                String minute = timeMatcher24Hr.group(2);
                String second = timeMatcher24Hr.group(3) != null ? timeMatcher24Hr.group(3) : "00";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.parse(todayStr + " " + hour + ":" + minute + ":" + second);
            }

            logger.debug("Could not parse time string: " + timeString);
            return null;

        } catch (ParseException e) {
            logger.debug("Error parsing time string: " + timeString + " " + e);
            return null;
        }
    }
}