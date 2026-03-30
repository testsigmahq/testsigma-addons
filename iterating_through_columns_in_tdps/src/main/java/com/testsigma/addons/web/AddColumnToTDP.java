package com.testsigma.addons.web;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "add new column column-name with default value default-value to TDP tdp-id" +
        " using the apikey api-key",
        description = "Adds a new parameter/column to an existing TDP. The new column is added" +
                " to every existing row with the specified default value. Uses PUT to fetch" +
                " existing data, add the column to all rows, and replace the full TDP data.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class AddColumnToTDP extends WebAction {

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
            String response = TDPApiUtil.addTDPColumn(tdpIdStr, colName, defValue, apiKeyStr, logger);

            logger.info("New column added to TDP successfully");
            setSuccessMessage("Successfully added new column <b>" + colName
                    + "</b> with default value <b>" + defValue + "</b> to all rows in TDP");
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Error occurred while adding column to TDP: " + e.getMessage());
            setErrorMessage("Failed to add column to TDP: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
