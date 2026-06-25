package com.testsigma.addons.ios;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Action(actionText = "Update TDP tdp-id set name set-name parameter parameter-name with value parameter-value" +
        " using the apikey api-key",
        description = "Updates the value of a specific parameter/column for a given set name in the TDP.",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class UpdateTDPValue extends IOSAction {

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
        String tdpIdStr = tdpId.getValue().toString().trim();
        String setNameStr = setName.getValue().toString().trim();
        String paramName = parameterName.getValue().toString().trim();
        String paramValue = parameterValue.getValue().toString().trim();
        String apiKeyStr = apiKey.getValue().toString().trim();
        try {
            Map<String, String> updatedData = new LinkedHashMap<>();
            updatedData.put(paramName, paramValue);
            TDPApiUtil.updateTDPIterationData(tdpIdStr, setNameStr, updatedData, apiKeyStr, logger);
            setSuccessMessage("Successfully updated parameter <b>" + paramName + "</b> to <b>" + paramValue + "</b> in set <b>" + setNameStr + "</b>");
            return Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Failed to update TDP value: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
