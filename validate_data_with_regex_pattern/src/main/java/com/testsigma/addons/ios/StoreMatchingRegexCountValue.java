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
        actionText = "Count matches for regex regex-pattern in Input-Data and store the count in runtime variable Variable-Name",
        description = "Counts the number of matches for the given regex in the input data and stores the count into the given runtime variable",
        applicationType = ApplicationType.IOS
)
public class StoreMatchingRegexCountValue extends IOSAction {

    @TestData(reference = "Input-Data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "regex-pattern")
    private com.testsigma.sdk.TestData regex;

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

        logger.info("Input data : " + inputData);
        logger.info("Regex : " + regexValue);
        logger.info("Runtime variable name : " + varName);

        try {
            Pattern pattern = Pattern.compile(regexValue);
            Matcher matcher = pattern.matcher(inputData);
            int matchCount = 0;

            while (matcher.find()) {
                matchCount++;
            }

            runTimeData.setKey(varName);
            runTimeData.setValue(String.valueOf(matchCount));

            setSuccessMessage("Regex '" + regexValue + "' matched " + matchCount +
                    " times and stored in variable '" + varName + "'");

            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception occurred " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to count regex matches: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
