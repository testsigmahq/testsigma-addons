package com.testsigma.addons.web;


import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestCaseResult;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.getStatusCodeData;

@Data
@Action(actionText = "Get the status code of the stored URL and store it in runtime variable status_code",
        description = "This action retrieves the status code of the stored URL.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB)
public class GetStatusCode extends WebAction {

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;
    @TestData(reference = "status_code", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData statusCodeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        try {
            Result result = Result.SUCCESS;
            int statusCodeFromFile = getStatusCodeData(testCaseResult.getId(), logger);
            if (statusCodeFromFile == -1) {
                setErrorMessage("Status code not found in the file.");
                return Result.FAILED;
            }
            logger.info("Status code retrieved: " + statusCodeFromFile);
            // Store the status code in the runtime variable
            runTimeData.setKey(statusCodeVariable.getValue().toString());
            runTimeData.setValue(String.valueOf(statusCodeFromFile));
            setSuccessMessage("Status code retrieved successfully: " + statusCodeFromFile);
            return result;
        } catch (Exception e) {
            logger.debug("Error retrieving status code: " + e.getMessage());
            setErrorMessage("Failed to retrieve status code: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
