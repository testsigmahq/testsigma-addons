package com.testsigma.addons.android;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import org.openqa.selenium.NoSuchElementException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

@Action(actionText = "Get entire row from TDP tdp-id for the set name set-name using the apikey api-key and" +
        " store data in the run time variable runtime-variable",
        description = "Get entire row from TDP",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class GetEntireRowFromTDP extends AndroidAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData4;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        try {
            String tdpId = testData1.getValue().toString();
            String setName = testData2.getValue().toString();
            String apiKey = testData3.getValue().toString();
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpId, setName, apiKey, logger);
            String resultVariable = "";
            for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                String key = entry.getKey();
                if (key.equals("S.No.") || key.equals("ETF") || key.equals("Set Name")) continue;
                resultVariable += entry.getValue() + ", ";
            }
            resultVariable = resultVariable.substring(0, resultVariable.length() - 2);
            runTimeData.setValue(resultVariable);
            runTimeData.setKey(testData4.getValue().toString());
            logger.info("Successfully retrieved and stored data for iteration: " + setName);
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            logger.warn("Error occurred while processing TDP data: " + ExceptionUtils.getMessage(e));
            setErrorMessage("Error occurred while processing TDP data: " + ExceptionUtils.getMessage(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
