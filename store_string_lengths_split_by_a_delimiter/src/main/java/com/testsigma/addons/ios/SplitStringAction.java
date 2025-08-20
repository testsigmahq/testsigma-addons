package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Split the string testdata by delimeter and store the count of string into a runtime variable",
        description = "This addon will Split the string testdata by delimeter and store the count of string into a runtime variable",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class SplitStringAction extends IOSAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "delimeter")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVar;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException
    {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;

        try
        {
            logger.debug("testdata => " + testData1.getValue().toString());
            logger.debug("delimeter => " + testData2.getValue().toString());
            logger.debug("Runtime_Variable => " + runtimeVar.getValue().toString());

            String ABS = testData1.getValue().toString();
            String[] strings = ABS.split(testData2.getValue().toString());
            int res = strings.length;
            logger.info("res " + res);

            runTimeData.setKey(runtimeVar.getValue().toString());
            runTimeData.setValue(String.valueOf(res));

            logger.info("Successfully stored the count of the given string " + testData1.getValue().toString() + " into a runtime variable " + runtimeVar.getValue().toString() + " = " + res);
            setSuccessMessage("Successfully stored the count of the given string " + testData1.getValue().toString() + " into a runtime variable " + runtimeVar.getValue().toString() + " = " + res);

        }
        catch(Exception e)
        {
            logger.warn("Exception while executing action - "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Operation failed , the error message is ::::"+ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }
        return result;
    }
}