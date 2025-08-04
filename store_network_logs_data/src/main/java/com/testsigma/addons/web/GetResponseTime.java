package com.testsigma.addons.web;


import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestCaseResult;
import lombok.Data;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.addons.web.utilities.ResponseDataUtilities;

@Data
@Action(actionText = "get the response time for the tracked endpoint and store it in runtime variable runtime_variable",
        description = "gets the response time of the tracked endpoint",
        applicationType = ApplicationType.WEB)
public class GetResponseTime extends WebAction {

    @TestData(reference = "runtime_variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {
        try {
            // get the response time from the response data utilities
            int responseTime = ResponseDataUtilities.getResponseTime(testCaseResult.getId(), logger);
            // store the response time in the runtime variable
            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(responseTime);
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.debug("Error in getting response time: " + e.getMessage());
            return Result.FAILED;
        }
    }

}
