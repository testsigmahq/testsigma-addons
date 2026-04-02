package com.testsigma.addons.web;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "add new row row-name to TDP tdp-id using the apikey api-key",
        description = "Adds a new row/set to an existing TDP with empty values for all existing parameters." +
                " Uses PUT to fetch existing data, append the new row with empty values, and replace the full TDP data.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class AddNewRowToTDP extends WebAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData tdpId;
    @TestData(reference = "row-name")
    private com.testsigma.sdk.TestData rowName;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating AddNewRowToTDP execution");
        String tdpIdStr = tdpId.getValue().toString().trim();
        String rowNameStr = rowName.getValue().toString().trim();
        String apiKeyStr = apiKey.getValue().toString().trim();
        logger.info("TDP ID: " + tdpIdStr + ", Row Name: " + rowNameStr);
        try {
            TDPApiUtil.addTDPRowWithEmptyData(tdpIdStr, rowNameStr, apiKeyStr, logger);
            logger.info("New row added to TDP successfully");
            setSuccessMessage("Successfully added new row <b>" + rowNameStr + "</b> to TDP with empty parameter values");
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.info("Error occurred while adding new row to TDP: " + e.getMessage());
            setErrorMessage("Failed to add new row to TDP: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
