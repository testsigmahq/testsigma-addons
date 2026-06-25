package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Store total row count of TDP tdp-id using the apikey api-key into variable row-count-variable",
        description = "Get total row count of TDP",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class GetTDPRowcount extends WebAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "row-count-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution com.testsigma.addons.mobileweb.GetTDPRowcount");
        try {
            String tdpId = testData1.getValue().toString();
            String apiKey = testData2.getValue().toString();
            int totalRowCount = TDPApiUtil.getTDPRowCount(tdpId, apiKey, logger);
            runTimeData.setKey(testData3.getValue().toString());
            runTimeData.setValue(String.valueOf(totalRowCount));
            logger.info("Total row count: " + totalRowCount);
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            logger.info("Error occurred while getting total row count of TDP: " + ExceptionUtils.getMessage(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
