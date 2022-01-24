package com.testsigma.addons.broken_link_finder.web;

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
@Action(actionText = "Check & report all console errors in url",
        description = "Collects any possible error message given by the browser for a given URL",
        applicationType = ApplicationType.WEB)
public class ReportErrorsInPage extends WebAction {

    @TestData(reference = "url")
    private com.testsigma.sdk.TestData URL;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try{
            driver.get(URL.getValue().toString());
            LogEntries logEntries = driver.manage().logs().get("browser");
            List<LogEntry> logEntryList = logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE)).collect(Collectors.toList());
            if (logEntryList.size() > 0) {
                setErrorMessage(" Errors [" + logEntryList.size() + "] : " + logEntryList.toArray());
                return Result.FAILED;
            } else {
                setSuccessMessage("There are no console errors in the page");
                return Result.SUCCESS;
            }
        }catch (Exception exception){
            setErrorMessage("error while finding Broken Images ");
            return Result.FAILED;
        }
    }
}