package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Replace test_data with space and store in variable runtime",
        description = "Replace test-data with space and store in variable runtime",
        applicationType = ApplicationType.WEB)
public class NewLineReplaceWithSpace extends WebAction {

    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "runtime")
    private com.testsigma.sdk.TestData runtimevalue;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            String str = testData.getValue().toString();
            logger.info("Element contains new line: " + str.contains("\n"));

            // Replace newlines, carriage returns, and tabs with space
            String str1 = str.replaceAll("[\\t\\n\\r]+", " ");

            runTimeData.setValue(str1);
            runTimeData.setKey(runtimevalue.getValue().toString());

            setSuccessMessage("Successfully stored after removing new line and replacing with space: " + str1);
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception while replacing new line with space: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to replace new line with space. Error: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
