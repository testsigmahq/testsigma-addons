
package com.testsigma.addons.web;

import com.testsigma.addons.utils.CheckboxActions;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Check all the checkboxes in the current page",
        description = "Check all the checkboxes in the current page",
        applicationType = ApplicationType.WEB)
public class CheckAllCheckboxes extends WebAction {

    @Override
    public Result execute() {
        try {
            logger.info("Starting to check all checkboxes on Web platform");
            new CheckboxActions().checkAllEnabledCheckboxes(driver, logger);
            logger.info("Successfully checked all the checkboxes in the current page on Web platform");
            setSuccessMessage("Successfully checked all the checkboxes in the current page.");
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while checking all the checkboxes in the current page: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in CheckAllCheckboxes Web action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

