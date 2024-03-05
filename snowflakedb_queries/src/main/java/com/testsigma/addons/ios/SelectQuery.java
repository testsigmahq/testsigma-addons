package com.testsigma.addons.ios;


import com.testsigma.addons.util.FetchResponseData;
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
@Action(actionText = "SnowflakeDB: Execute Select Query query-string on the Connection db-connection-url and store the result into a runtime variable variable-name",
        description = "This action executes given Select query against the connection provided and stores the response in a runtime variable",
        applicationType = ApplicationType.IOS)
public class SelectQuery extends WebAction {

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
        String result = "";
        try {
            result = FetchResponseData.execute(dbConnectionUrl.getValue().toString(), query.getValue().toString(), logger);

            if (!result.isEmpty()) {
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(result);
                runTimeData.setKey(variableName.getValue().toString());
                logger.info("Response data: "+ result);
                logger.info("Successfully retrieved result of the given query and stored in runtime variable variable-name: " + variableName.getValue().toString());
            } else {
                setErrorMessage("Response data is empty...");
                logger.info("Response data is empty, response length: " + result.length());
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
           setErrorMessage("Error occurred while executing the given query: " + errorMessage);
            return Result.FAILED;
        }
        if (result.isEmpty()) {
            return Result.FAILED;
        }
        setSuccessMessage("Successfully retrieved result of the given query and stored in runtime variable variable-name: " + variableName.getValue().toString());
        return Result.SUCCESS;
    }
}