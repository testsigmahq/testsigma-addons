package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Action(actionText = "Add new column column-name with default value default-value to TDP tdp-id" +
        " using the apikey api-key",
        description = "Adds a new parameter/column to an existing TDP with the specified default value for all rows.",
        applicationType = ApplicationType.MOBILE_WEB,
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
        String colName = columnName.getValue().toString().trim();
        if (colName.isEmpty()) {
            setErrorMessage("Column name cannot be empty");
            return Result.FAILED;
        }
        try {
            TDPApiUtil.addTDPColumn(tdpId.getValue().toString().trim(), colName, defaultValue.getValue().toString().trim(), apiKey.getValue().toString().trim(), logger);
            setSuccessMessage("Successfully added new column <b>" + colName + "</b> with default value <b>" + defaultValue.getValue() + "</b> to all rows in TDP");
            return Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Failed to add column to TDP: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }
}
