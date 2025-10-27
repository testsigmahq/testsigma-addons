
package com.testsigma.addons.web;

import com.testsigma.addons.utils.CheckboxActions;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Uncheck all the checkboxes in the current page",
        description = "Uncheck all the checkboxes in the current page",
        applicationType = ApplicationType.WEB)
public class UnCheckAllCheckboxes extends WebAction {

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            new CheckboxActions().unCheckAllEnabledCheckboxes(driver, logger);
            logger.info("Successfully unchecked all the checkboxes in the current page.");
            setSuccessMessage("Successfully unchecked all the checkboxes in the current page.");
            return Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("An error occurred while unchecking all the checkboxes in the current page: " + e.getMessage());
            logger.info("An error occurred while unchecking all the checkboxes in the current page: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

}

