package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;

import java.util.LinkedHashMap;
import java.util.Map;

@Action(actionText = "update TDP tdp-id set name set-name parameter parameter-name with value parameter-value" +
        " using the apikey api-key",
        description = "Updates the value of a specific parameter/column for a given set name in the TDP.",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class UpdateTDPValue extends WebAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData tdpId;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData setName;
    @TestData(reference = "parameter-name")
    private com.testsigma.sdk.TestData parameterName;
    @TestData(reference = "parameter-value")
    private com.testsigma.sdk.TestData parameterValue;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating UpdateTDPValue execution");
        try {
            Map<String, String> updatedData = new LinkedHashMap<>();
            updatedData.put(parameterName.getValue().toString().trim(), parameterValue.getValue().toString().trim());
            TDPApiUtil.updateTDPIterationData(tdpId.getValue().toString().trim(), setName.getValue().toString().trim(), updatedData, apiKey.getValue().toString().trim(), logger);
            setSuccessMessage("Successfully updated parameter <b>" + parameterName.getValue() + "</b> to <b>" + parameterValue.getValue() + "</b> in set <b>" + setName.getValue() + "</b>");
            return Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Failed to update TDP value: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
