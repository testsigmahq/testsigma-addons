package com.testsigma.addons.restapi;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;


@Data
@Action(actionText = "Split the given-string with the regex-condition and store the value from the position in the output-variable",
        description = "To split the given string using the regex condition and to extract the value from specified position",
        applicationType = ApplicationType.REST_API)
public class SplitStringAction extends RestApiAction {

    private String actualValue = null;
    private int index;

    @TestData(reference = "given-string")
    private com.testsigma.sdk.TestData inputValue;

    @TestData(reference = "regex-condition")
    private com.testsigma.sdk.TestData regex;

    @TestData(reference = "position")
    private com.testsigma.sdk.TestData position;
    
    @TestData(reference = "output-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData extractedString;
    
    @com.testsigma.sdk.annotation.RunTimeData
    private RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;

        String value = inputValue.getValue().toString();
        String delimiter = regex.getValue().toString();
        try {
            index = Integer.parseInt(position.getValue().toString()) - 1;
            String[] arrOfStr = value.split(delimiter);
            actualValue = String.valueOf(arrOfStr[index]);
            runTimeData = new RunTimeData();
            runTimeData.setValue(actualValue);
            runTimeData.setKey(extractedString.getValue().toString());
            logger.info("The extracted value is:" +" "+ actualValue + " " +"and stored into: " + extractedString.getValue() + " variable");
        } catch (NumberFormatException error) {
            result = Result.FAILED;
            logger.info(ExceptionUtils.getStackTrace(error));
            setErrorMessage("Position accepts only numeric values");
            return result;
        } catch (ArrayIndexOutOfBoundsException error) {
            result = Result.FAILED;
            logger.info(ExceptionUtils.getStackTrace(error));
            setErrorMessage("Value entered for position should not exceed length of the split array");
            return result;
        } catch (Exception error) {
            logger.info(ExceptionUtils.getStackTrace(error));
            setErrorMessage("Failed to split the string using the regex.");
            result = Result.FAILED;
            return result;
        }
        setSuccessMessage("Extracted value from the string is:"+" "+ actualValue);
        return result;
    }
}