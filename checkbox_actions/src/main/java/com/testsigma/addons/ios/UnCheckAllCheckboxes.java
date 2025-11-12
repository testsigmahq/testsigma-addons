
package com.testsigma.addons.ios;

import com.testsigma.addons.utils.CheckboxActions;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Uncheck all the checkboxes in the current page",
        description = "Uncheck all the checkboxes in the current page",
        applicationType = ApplicationType.IOS)
public class UnCheckAllCheckboxes extends IOSAction {

    @Override
    public Result execute() {
        try {
            logger.info("Starting to uncheck all checkboxes on iOS platform");
            IOSDriver iosDriver = (IOSDriver) this.driver;
            new CheckboxActions().unCheckAllEnabledCheckboxes(iosDriver, logger);
            logger.info("Successfully unchecked all the checkboxes in the current page on iOS platform");
            setSuccessMessage("Successfully unchecked all the checkboxes in the current page.");
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while unchecking all the checkboxes in the current page: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in UnCheckAllCheckboxes iOS action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

