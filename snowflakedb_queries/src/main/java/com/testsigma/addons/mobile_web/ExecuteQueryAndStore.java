package com.testsigma.addons.mobile_web;


import com.testsigma.addons.util.FetchResponse;
import com.testsigma.addons.util.ResponseData;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "SnowflakeDB: Execute Query query-string on the Connection db-connection-url and store the result into a runtime variable variable-name",
        description = "This action executes given query against the connection provided and stores the response in a runtime variable",
        applicationType = ApplicationType.MOBILE_WEB)
public class ExecuteQueryAndStore extends WebAction {

    @TestData(reference = "query-string")
    private com.testsigma.sdk.TestData query;
    @TestData(reference = "db-connection-url")
    private com.testsigma.sdk.TestData dbConnectionUrl;
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        String query1 = query.getValue().toString();
        ResponseData result = null;
        try {
            result = FetchResponse.execute(dbConnectionUrl.getValue().toString(), query.getValue().toString(), logger);
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variableName.getValue().toString());
            if (query1.toLowerCase().startsWith("create")) {
                Pattern pattern = Pattern.compile("Table\\s(\\w+)\\ssuccessfully\\screated\\.", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(result.getValue());
                if (matcher.find()) {
                    String tableName = matcher.group(1);
                    logger.info("Table name: " + tableName);
                    runTimeData.setValue(tableName);
                } else {
                    runTimeData.setValue("Not Found");
                    logger.info("Table name not found in the query.");
                }
            } else if (query1.toLowerCase().startsWith("alter") && result.getValue().contains("successfully")) {
                String[] arr = query1.split(" ");
                 runTimeData.setValue(arr[2].toUpperCase());
            } else {
                runTimeData.setValue(result.getValue());
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage("Error occurred while executing the given query: " + errorMessage);
            return Result.FAILED;
        }
        if (Objects.requireNonNull(result).getResponseString().isEmpty()) {
            setErrorMessage("Response is null");
            logger.info("Response Object is empty");
            return Result.FAILED;
        }
        setSuccessMessage("Successfully executed given query: " + result.getResponseString() + "and result value stored in runtime variable variable-name: " + variableName.getValue().toString());
        logger.info("Response: " + result.getResponseString());
        return Result.SUCCESS;
    }
}
