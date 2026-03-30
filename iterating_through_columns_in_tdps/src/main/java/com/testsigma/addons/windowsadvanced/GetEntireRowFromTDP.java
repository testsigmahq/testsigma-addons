package com.testsigma.addons.windowsadvanced;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;

import java.util.Map;

@Action(actionText = "get entire row from TDP tdp-id for the set name set-name using the apikey api-key and" +
        " store data in the run time variable runtime-variable",
        description = "Get entire row from TDP",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Get entire row from TDP",
        useCustomScreenshot = false)
public class GetEntireRowFromTDP extends WindowsAdvancedAction {

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
    protected com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        try {
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(testData1.getValue().toString(), testData2.getValue().toString(), testData3.getValue().toString(), logger);
            String resultVariable = "";
            for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                if (entry.getKey().equals("S.No.") || entry.getKey().equals("ETF") || entry.getKey().equals("Set Name")) continue;
                resultVariable += entry.getValue() + ", ";
            }
            resultVariable = resultVariable.substring(0, resultVariable.length() - 2);
            runTimeData.setValue(resultVariable);
            runTimeData.setKey(testData4.getValue().toString());
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Error occurred while processing TDP data: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
