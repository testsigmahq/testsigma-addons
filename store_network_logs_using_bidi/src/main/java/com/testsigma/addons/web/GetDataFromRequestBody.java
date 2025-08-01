package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestCaseResult;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.getSpecificHeaderValue;


@Data
@Action(actionText = "bidi : get value of the request header from the attribute header_key  " +
        "and store the value in runtime variable variable_name",
        description = "Extracts header value from network request and stores it in runtime variable, please make " +
                "sure that the request type is POST or PUT.",
        applicationType = ApplicationType.WEB)
public class GetDataFromRequestBody extends WebAction {

    @TestData(reference = "url_value")
    private com.testsigma.sdk.TestData urlValue;

    @TestData(reference = "method_value")
    private com.testsigma.sdk.TestData methodValue;

    @TestData(reference = "header_key")
    private com.testsigma.sdk.TestData headerKey;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {
        logger.info("Execution started for action: GetDataFromrequestBody");

        try {
            // Step 1: Get the specific header value
            logger.info("Getting header value for key: " + headerKey.getValue().toString() +
                    " from test case result ID: " + testCaseResult.getId());
            String headerValue;
            try {
                headerValue = getSpecificHeaderValue(testCaseResult.getId(),
                        headerKey.getValue().toString(), logger);
            } catch (IllegalStateException e) {
                throw new Exception("Failed to retrieve header value. The network request from the preceding" +
                        " step might be missing or empty.", e);
            }

            if (headerValue == null || headerValue.trim().isEmpty()) {
                throw new Exception("Retrieved header value is empty. Cannot proceed.");
            }
            logger.info("Header value successfully retrieved: " + headerValue);

            // Step 2: Store the header value in runtime data
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(headerValue);

            logger.info("Successfully extracted header value and stored it in runtime variable: " +
                    runTimeData.getValue().toString());
            setSuccessMessage("Header value fetched and stored in runtime variable: "
                    + runTimeData.getValue().toString());

        } catch (Exception e) {
            logger.warn("Exception occurred during execution: " + e.getMessage());
            setErrorMessage("Exception occurred while fetching data from request headers: " + e.getMessage());
            return Result.FAILED;
        }
        return Result.SUCCESS;
    }
}


