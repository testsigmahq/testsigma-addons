package com.testsigma.addons.developer_console_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.logging.LogEntry;

import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
@Action(
        actionText = "Testing Store unique console log errors in runtime variable variable-name",
        description = "Stores the unique console log errors in runtime variable",
        applicationType = ApplicationType.WEB
)
public class StoreUniqueConsoleLogErrors extends WebAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;

        try {
            // Fetch browser logs
            var logEntries = driver.manage().logs().get("browser").getAll();

            // Filter SEVERE logs and extract JUST the error message
            Set<String> uniqueErrors = logEntries.stream()
                    .filter(entry -> entry.getLevel().equals(Level.SEVERE))
                    .map(LogEntry::getMessage)                // only the message
                    .map(msg -> msg.replaceAll("\\s+", " ").trim()) // clean formatting
                    .collect(Collectors.toSet());

            logger.info("Unique console errors found: " + uniqueErrors);

            // Store into runtime variable
            runtimeData.setKey(testData.getValue().toString());
            runtimeData.setValue(String.join(" || ", uniqueErrors));

            setSuccessMessage("Successfully stored unique console log errors: " + uniqueErrors);

        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to store console log errors: " + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }

        return result;
    }
}
