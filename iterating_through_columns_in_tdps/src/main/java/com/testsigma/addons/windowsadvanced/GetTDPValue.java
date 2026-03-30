package com.testsigma.addons.windowsadvanced;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import java.util.Map;

@Action(actionText = "get TDP tdp-id value for set name set-name and parameter parameter-name" +
        " using the apikey api-key and store in variable runtime-variable",
        description = "Gets the value of a specific parameter/column for a given set name in the TDP" +
                " and stores it in a runtime variable.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Get TDP parameter value for a set",
        useCustomScreenshot = false)
public class GetTDPValue extends WindowsAdvancedAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData tdpId;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData setName;
    @TestData(reference = "parameter-name")
    private com.testsigma.sdk.TestData parameterName;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() {
        logger.info("Initiating GetTDPValue execution");
        String tdpIdStr = tdpId.getValue().toString().trim();
        String setNameStr = setName.getValue().toString().trim();
        String paramName = parameterName.getValue().toString().trim();
        String apiKeyStr = apiKey.getValue().toString().trim();
        try {
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpIdStr, setNameStr, apiKeyStr, logger);
            if (!parameterValues.containsKey(paramName)) {
                setErrorMessage("Parameter <b>" + paramName + "</b> not found in set <b>" + setNameStr + "</b>. Available parameters: " + parameterValues.keySet());
                return Result.FAILED;
            }
            String value = parameterValues.get(paramName);
            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(value);
            setSuccessMessage("Successfully retrieved parameter <b>" + paramName + "</b> = <b>" + value + "</b> from set <b>" + setNameStr + "</b>");
            return Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Failed to get TDP value: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
