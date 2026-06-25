package com.testsigma.addons.android;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

@Action(actionText = "Store total column count of TDP tdp-id for the set name set-name using the apikey" +
        " api-key into variable column-count-variable",
        description = "Get total column count of TDP",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class GetTDPColumncount extends AndroidAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "column-count-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData4;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        try {
            String tdpId = testData1.getValue().toString();
            String setName = testData2.getValue().toString();
            String apiKey = testData3.getValue().toString();
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpId, setName, apiKey, logger);
            int totalColumnCount = parameterValues.size();
            runTimeData.setKey(testData4.getValue().toString());
            runTimeData.setValue(String.valueOf(totalColumnCount));
            logger.info("Total column count: " + totalColumnCount);
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            logger.info("Error occurred while getting total column count of TDP: " + ExceptionUtils.getMessage(e));
            setErrorMessage("Error occurred while getting total column count: " + ExceptionUtils.getMessage(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
