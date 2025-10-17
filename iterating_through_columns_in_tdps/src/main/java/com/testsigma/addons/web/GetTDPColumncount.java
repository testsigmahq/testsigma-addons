package com.testsigma.addons.web;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;

import java.util.Map;


@Action(actionText = "store total column count of TDP tdp-id for the set name set-name using the apikey" +
        " api-key into variable column-count-variable",
        description = "Get total column count of TDP",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class GetTDPColumncount extends WebAction {
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
        logger.info("Initiating execution com.testsigma.addons.web.GetTotalColumnCountOfTDP");
        try {
            String tdpId = testData1.getValue().toString();
            String setName = testData2.getValue().toString();
            String apiKey = testData3.getValue().toString();
            logger.info("TDP ID: "+ tdpId +", Set Name: "+ setName +", API Key: "+ apiKey);
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpId, setName, apiKey);
            logger.info("Parameter values: " + parameterValues);
            int totalColumnCount = parameterValues.entrySet().size();

            runTimeData.setKey(testData4.getValue().toString());
            runTimeData.setValue(String.valueOf(totalColumnCount));
            logger.info("Total column count: " + totalColumnCount);
            logger.info("Stored total column count in runtime variable: " + totalColumnCount);
            return com.testsigma.sdk.Result.SUCCESS;
        }
        catch (Exception e) {
            logger.info("Error occurred while getting total column count of TDP: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
