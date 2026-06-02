package com.testsigma.addons.android;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "add new column column-name with default value default-value to TDP tdp-id" +
        " using the apikey api-key",
        description = "Adds a new parameter/column to an existing TDP with the specified default value for all rows.",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class AddColumnToTDP extends AndroidAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData tdpId;
    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData columnName;
    @TestData(reference = "default-value")
    private com.testsigma.sdk.TestData defaultValue;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData apiKey;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating AddColumnToTDP execution");
        String tdpIdStr = tdpId.getValue().toString().trim();
        String colName = columnName.getValue().toString().trim();
        String defValue = defaultValue.getValue().toString().trim();
        String apiKeyStr = apiKey.getValue().toString().trim();
        logger.info("TDP ID: " + tdpIdStr + ", Column: " + colName + ", Default Value: " + defValue);
        if (colName.isEmpty()) {
            setErrorMessage("Column name cannot be empty");
            return Result.FAILED;
        }
        try {
            TDPApiUtil.addTDPColumn(tdpIdStr, colName, defValue, apiKeyStr, logger);
            logger.info("New column added to TDP successfully");
            setSuccessMessage("Successfully added new column <b>" + colName + "</b> with default value <b>" + defValue + "</b> to all rows in TDP");
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.info("Error occurred while adding column to TDP: " + e.getMessage());
            setErrorMessage("Failed to add column to TDP: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
