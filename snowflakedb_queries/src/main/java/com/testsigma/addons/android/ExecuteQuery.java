package com.testsigma.addons.android;


import com.testsigma.addons.util.FetchResponse;
import com.testsigma.addons.util.ResponseData;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "SnowflakeDB: Execute Query query-string on the Connection db-connection-url",
        description = "This action executes given query against the connection provided and prints the response",
        applicationType = ApplicationType.ANDROID)
public class ExecuteQuery extends WebAction {

    @TestData(reference = "query-string")
    private com.testsigma.sdk.TestData query;
    @TestData(reference = "db-connection-url")
    private com.testsigma.sdk.TestData dbConnectionUrl;

    @Override
    protected Result execute() throws NoSuchElementException {
        ResponseData result = null;
        try {
            result = FetchResponse.execute(dbConnectionUrl.getValue().toString(), query.getValue().toString(), logger);
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage("Error occurred while executing the given query: "+ errorMessage);
            return Result.FAILED;
        }
        if (result == null){
            return Result.FAILED;
        }
        setSuccessMessage("Successfully executed given query: "+ result.getResponseString());
        logger.info("Response-string: "+result.getResponseString());
        return Result.SUCCESS;
    }
}
