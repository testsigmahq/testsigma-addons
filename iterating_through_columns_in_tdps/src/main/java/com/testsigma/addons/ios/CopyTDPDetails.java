package com.testsigma.addons.ios;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Action(actionText = "Copy all rows from reference TDP reference-tdp-id to target TDP target-tdp-id using apikey api-key",
        description = "Copies all rows from a reference TDP into a target TDP. Appends every row from the reference TDP to the target TDP, preserving row names and data.",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class CopyTDPDetails extends IOSAction {

    @TestData(reference = "reference-tdp-id")
    private com.testsigma.sdk.TestData referenceTdpId;

    @TestData(reference = "target-tdp-id")
    private com.testsigma.sdk.TestData targetTdpId;

    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating CopyTDPDetails execution");
        String referenceTdpIdStr = referenceTdpId.getValue().toString().trim();
        String targetTdpIdStr = targetTdpId.getValue().toString().trim();
        String apiKeyStr = apiKey.getValue().toString().trim();
        logger.info("Reference TDP ID: " + referenceTdpIdStr + ", Target TDP ID: " + targetTdpIdStr);
        try {
            TDPApiUtil.copyTDPRows(referenceTdpIdStr, targetTdpIdStr, apiKeyStr, logger);
            logger.info("Successfully copied all rows from reference TDP to target TDP");
            setSuccessMessage("Successfully copied all rows from reference TDP <b>" + referenceTdpIdStr
                    + "</b> into target TDP <b>" + targetTdpIdStr + "</b>");
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            logger.info("Error occurred while copying TDP rows: " + ExceptionUtils.getMessage(e));
            setErrorMessage("Failed to copy TDP rows: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
