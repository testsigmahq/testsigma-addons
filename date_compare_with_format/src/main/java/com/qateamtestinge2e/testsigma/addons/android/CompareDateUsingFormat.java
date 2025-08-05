package com.qateamtestinge2e.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Action(actionText = "Verify date testdata1 is Equals/Less Than/Greater Than/Greater Than or equals/Less Than or equals testdata2 with date format",
        description = "Verify if one date is equal to, less than, or greater than another date",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class CompareDateUsingFormat extends AndroidAction {
    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData firstDateTestData;
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData secondDateTestData;
    @TestData(reference = "Equals/Less Than/Greater Than/Greater Than or equals/Less Than or equals" ,allowedValues = {"equals","less than","greater than","greater than or equals","less than or equals"} )
    private com.testsigma.sdk.TestData operatorTestData;
    @TestData(reference = "date format")
    private com.testsigma.sdk.TestData dateFormatTestData;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        String firstDateString = firstDateTestData.getValue().toString();
        String secondDateString = secondDateTestData.getValue().toString();
        String operator = operatorTestData.getValue().toString().toLowerCase();
        String dateFormat = dateFormatTestData.getValue().toString();

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        Result result = Result.SUCCESS;
        try {
            Date firstDate = sdf.parse(firstDateString);
            Date secondDate = sdf.parse(secondDateString);

            int comparisonResult = firstDate.compareTo(secondDate);
            boolean isTrue = false;

            switch (operator) {
                case "equals":
                    isTrue = comparisonResult == 0;
                    break;
                case "less than":
                    isTrue = comparisonResult < 0;
                    break;
                case "less than or equals":
                    isTrue = comparisonResult <= 0;
                    break;
                case "greater than":
                    isTrue = comparisonResult > 0;
                    break;
                case "greater than or equals":
                    isTrue = comparisonResult >= 0;
                    break;
                default:
                    logger.warn("Invalid operator provided.");
                    setErrorMessage("Invalid operator provided.");
                    return Result.FAILED;
            }

            if (isTrue) {
                result = Result.SUCCESS;
                logger.info("Date comparison successful: " + firstDateString + " " + operator + " " + secondDateString);
                setSuccessMessage("Date comparison successful: " + firstDateString + " " + operator + " " + secondDateString);

            } else {
                result = Result.FAILED;
                logger.info("Date comparison failed: " + firstDateString + " not " + operator + " " + secondDateString);
                setErrorMessage("Date comparison failed: " + firstDateString + " not " + operator + " " + secondDateString);

            }
        } catch (ParseException e) {
            logger.warn("Exception occurred while comparing dates: " + e.getMessage());
            setErrorMessage("Please check the given Date format according to the input Date");
            result = Result.FAILED;
        } catch (Exception error) {
            result = Result.FAILED;
            logger.warn("Exception occurred while comparing dates: " + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Exception occurred while comparing dates. Error: " + ExceptionUtils.getMessage(error));
        }
        return result;
    }

}

