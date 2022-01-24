package com.testsigma.addons.developer_console_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
@Action(actionText = "Verify there are no errors on console",
        description = "Verifies if no errors are found in the developer console and return the status",
        applicationType = ApplicationType.WEB)
public class VerifyThereAreNoErrorsOnConsole extends WebAction {

    private static final String SUCCESS_MESSAGE = "There are no errors in the console";
    private static final String ERROR_MESSAGE = "There are errors in the console";

    @Override
    public Result execute() throws NoSuchElementException {
        LogEntries logEntries = driver.manage().logs().get("browser");
        List<LogEntry> logEntryList = logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE)).collect(Collectors.toList());
        if (logEntryList.size() > 0) {
            setErrorMessage(ERROR_MESSAGE);
            return Result.FAILED;
        } else {
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        }
    }
}

