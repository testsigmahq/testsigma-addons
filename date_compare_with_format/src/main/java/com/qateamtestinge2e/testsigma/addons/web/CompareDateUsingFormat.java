package com.qateamtestinge2e.testsigma.addons.web;

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
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class CompareDateUsingFormat extends WebAction {
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
        sdf.setLenient(false);


        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
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
                    return com.testsigma.sdk.Result.FAILED;
            }

            if (isTrue) {
                result = Result.SUCCESS;
                logger.info("Date comparison successful: " + firstDateString + " " + operator + " " + secondDateString);
                setSuccessMessage("Date comparison successful: " + firstDateString + " " + operator + " " + secondDateString);

            } else {
                result = com.testsigma.sdk.Result.FAILED;
                logger.info("Date comparison failed: " + firstDateString + " not " + operator + " " + secondDateString+" Else part");
                setErrorMessage("Date comparison failed: " + firstDateString + " not " + operator + " " + secondDateString);

            }
        } catch (ParseException e) {
            logger.warn("Exception occurred while comparing dates: " + e.getMessage());
            setErrorMessage("Please check the given Date format according to the input Date");
            result = com.testsigma.sdk.Result.FAILED;
        }
         catch (Exception e) {
            logger.warn("Exception occurred while comparing dates: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while comparing dates. Error: " + ExceptionUtils.getMessage(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }

    }

