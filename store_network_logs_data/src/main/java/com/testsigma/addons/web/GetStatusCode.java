package com.testsigma.addons.web;


import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestCaseResult;
import lombok.Data;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.getStatusCodeData;

@Data
@Action(actionText = "test q1: get the status code of the stored URL",
        description = "This action retrieves the status code of the stored URL.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB)
public class GetStatusCode extends WebAction {

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

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
            setSuccessMessage("Status code retrieved successfully: " + statusCodeFromFile);
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.debug("Error retrieving status code: " + e.getMessage());
            setErrorMessage("Failed to retrieve status code: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
