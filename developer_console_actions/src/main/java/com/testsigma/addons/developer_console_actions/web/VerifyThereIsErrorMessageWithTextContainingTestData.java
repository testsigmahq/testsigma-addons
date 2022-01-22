package com.testsigma.addons.developer_console_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
@Action(actionText = "Verify there is error message with text containing test-data",
        description = "Verifies if a given error is found in the developer console",
        applicationType = ApplicationType.WEB)
public class VerifyThereIsErrorMessageWithTextContainingTestData extends WebAction {

    private static final String SUCCESS_MESSAGE = "There is an error message containing the expected text";
    private static final String ERROR_MESSAGE = "There are no error messages containing the expected text";

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        LogEntries logEntries = driver.manage().logs().get("browser");
        List<LogEntry> logEntryList = logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE) && logEntry.getMessage().contains(testData.getValue().toString())).collect(Collectors.toList());
        System.out.println(logEntryList);
        if (logEntryList.size() > 0) {
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        } else {
            setErrorMessage(ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

