package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(
        actionText = "Extract data matching regex and store to a variable, Regex: regex-pattern, Occurrence: occurrence-value, Input String: Input-Data, Store Variable Variable-Name",
        description = "Extracts the value matching the given regex from input data with the occurrence and stores it into the given runtime variable",
        applicationType = ApplicationType.IOS
)
public class StoreMatchingRegexValueInAVariableOccurrence extends IOSAction {

    @TestData(reference = "Input-Data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "regex-pattern")
    private com.testsigma.sdk.TestData regex;

    @TestData(reference = "occurrence-value")
    private com.testsigma.sdk.TestData occurrence;

    @TestData(reference = "Variable-Name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runTimeVar;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        String inputData = testData.getValue().toString();
        String regexValue = regex.getValue().toString();
        String varName = runTimeVar.getValue().toString();
        int occurrenceValue = Integer.parseInt(occurrence.getValue().toString());

        logger.info("Input data : " + inputData);
        logger.info("Regex : " + regexValue);
        logger.info("Runtime variable name : " + varName);

        try {
            Pattern pattern = Pattern.compile(regexValue);
            Matcher matcher = pattern.matcher(inputData);

            String matchedString = null;
            int matchCount = 0;

            while (matcher.find()) {
                matchCount++;
                if (matchCount == occurrenceValue) {
                    matchedString = matcher.group();
                    logger.info("Matched value found : " + matchedString);
                    break;
                }
            }

            if (matchedString == null) {
                setErrorMessage("Matching value with given regex is not found in input data");
                return Result.FAILED;
            }

            runTimeData.setKey(varName);
            runTimeData.setValue(matchedString);
            System.out.println("Matched string : " + matchedString);

            setSuccessMessage("Successfully stored matched value '" + matchedString +
                    "' into variable '" + varName + "'");

            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception occurred " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to extract regex match : " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
