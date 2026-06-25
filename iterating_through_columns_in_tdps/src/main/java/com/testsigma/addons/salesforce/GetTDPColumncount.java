package com.testsigma.addons.salesforce;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.SalesforceAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

@Action(actionText = "Store total column count of TDP tdp-id for the set name set-name using the apikey" +
        " api-key into variable column-count-variable",
        description = "Get total column count of TDP",
        applicationType = ApplicationType.Salesforce,
        useCustomScreenshot = false)
public class GetTDPColumncount extends SalesforceAction {

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
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(testData1.getValue().toString(), testData2.getValue().toString(), testData3.getValue().toString(), logger);
            int totalColumnCount = parameterValues.size();
            runTimeData.setKey(testData4.getValue().toString());
            runTimeData.setValue(String.valueOf(totalColumnCount));
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Error occurred while getting total column count: " + ExceptionUtils.getMessage(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
