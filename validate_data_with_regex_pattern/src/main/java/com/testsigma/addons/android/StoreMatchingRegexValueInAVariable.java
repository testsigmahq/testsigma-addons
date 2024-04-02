package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText ="Extract data matching regex and store to a variable, Regex: regex-pattern, Input String: Input-Data, Store Variable Variable-Name",
        description = "Extracts the regex matching data from given value and stores the first matching data in given variable",
        applicationType = ApplicationType.ANDROID)
public class StoreMatchingRegexValueInAVariable extends AndroidAction {

    @TestData(reference = "Input-Data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "regex-pattern")
    private com.testsigma.sdk.TestData regex;

    @TestData(reference = "Variable-Name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runTimeVar;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        String Regex = regex.getValue().toString();
        String data = testData.getValue().toString();
        String varName = runTimeVar.getValue().toString();
        logger.info("Given test data : "+ data);
        logger.info("Given regex-value : "+ Regex);
        logger.info("Given runtime variable name : "+varName);
        Result result = Result.SUCCESS;
        try {
            Pattern pattern = Pattern.compile(Regex);
            Matcher matcher = pattern.matcher(data);
            String matchedString;
            if (matcher.find()) {
                matchedString =  matcher.group();
                logger.info("Found the matched data : "+matcher.group());
            } else {
                setErrorMessage("Matching data with given regex is not found in given data");
                return Result.FAILED;
            }
            runTimeData.setKey(varName);
            runTimeData.setValue(matchedString);
            setSuccessMessage("Successfully stored the value : "+matchedString+", into given variable :"+varName);
        }
        catch(Exception e) {
            logger.debug(e.getMessage());
            setErrorMessage("Failed to perform operation "+e.getMessage()+e.getCause());
        }

        return result;
    }
}