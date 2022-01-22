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
@Action(actionText = "Verify there are no warnings on console",
        description = "Diagnose if there are any browser dev console warnings, and if there is, return the status",
        applicationType = ApplicationType.WEB)
public class VerifyThereAreNoWarningsOnConsole extends WebAction {

    private static final String SUCCESS_MESSAGE = "There are no warning in the console";
    private static final String ERROR_MESSAGE = "There are warning in the console";

    @Override
    public Result execute() throws NoSuchElementException {
        LogEntries logEntries = driver.manage().logs().get("browser");
        List<LogEntry> logEntryList = logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.WARNING)).collect(Collectors.toList());
        if (logEntryList.size() > 0) {
            setErrorMessage(ERROR_MESSAGE);
            return Result.FAILED;
        } else {
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        }
    }
}

