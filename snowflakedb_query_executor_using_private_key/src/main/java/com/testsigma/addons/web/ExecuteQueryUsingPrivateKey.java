package com.testsigma.addons.web;


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

import static com.testsigma.addons.util.SnowflakeDBConnection.P8FileCreator;

@Data
@Action(actionText = "SnowflakeDB: Execute Query query-string on the Connection db-connection-url, private key private-key (Starts with BEGIN PRIVATE KEY)",
        description = "This action executes given query against the connection provided and prints the response",
        applicationType = ApplicationType.WEB)
public class ExecuteQueryUsingPrivateKey extends WebAction {

    @TestData(reference = "query-string")
    private com.testsigma.sdk.TestData query;
    @TestData(reference = "db-connection-url")
    private com.testsigma.sdk.TestData dbConnectionUrl;
    @TestData(reference = "private-key")
    private com.testsigma.sdk.TestData privateKey;

    @Override
    protected Result execute() throws NoSuchElementException {
        ResponseData result = null;
        String privateKey1 = privateKey.getValue().toString();
        String dbConnectionUrl1 = dbConnectionUrl.getValue().toString() + "&private_key_file=" + P8FileCreator(privateKey1);
        try {
            result = FetchResponse.execute(dbConnectionUrl1, query.getValue().toString(), logger);
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage("Error occurred while executing the given query: "+ errorMessage);
            return Result.FAILED;
        }
        if (result == null){
            logger.info("Result is null");
            setErrorMessage("Error occurred while executing the given query: Fetched Result is null");
            return Result.FAILED;
        }
        setSuccessMessage("Successfully executed given query: "+ result.getResponseString());
        logger.info("Response-string: "+result.getResponseString());
        return Result.SUCCESS;
    }
}
