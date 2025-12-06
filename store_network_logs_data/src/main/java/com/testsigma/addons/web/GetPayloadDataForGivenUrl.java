package com.testsigma.addons.web;


import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestCaseResult;
import lombok.Data;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.ApplicationType;
import com.google.gson.JsonArray;

import com.testsigma.sdk.WebAction;
import com.testsigma.addons.web.utilities.ResponseDataUtilities;


@Data
@Action(actionText = "Get the payload data and store the content in runtime variable runtime_variable",
        description = "stores the payload data for the tracked url",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)

public class GetPayloadDataForGivenUrl extends WebAction {

    @TestData(reference = "runtime_variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {
        logger.info("Execution started for action: GetPayloadDataForGivenUrl");

        try {
            // get the payload data from the response data utilities

            JsonArray payloadData = ResponseDataUtilities.getPayloadData(testCaseResult.getId(), logger);
            String payloadDataString = payloadData.toString();

            // store the payload data in the runtime variable
            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(payloadDataString);

            logger.info("Payload data stored in runtime variable: " + payloadDataString);
            setSuccessMessage("Payload data stored in runtime variable: " + payloadDataString);

            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Error in getting payload data: " + e.getMessage());
            setErrorMessage("Error in getting payload data: " + e.getMessage());
            return Result.FAILED;
        }
    }


}
