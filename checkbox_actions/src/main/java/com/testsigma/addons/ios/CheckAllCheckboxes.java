
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
@Action(actionText = "Check all the checkboxes in the current page",
        description = "Check all the checkboxes in the current page",
        applicationType = ApplicationType.IOS)
public class CheckAllCheckboxes extends IOSAction {

    @Override
    public Result execute() {
        try {
            logger.info("Starting to check all checkboxes on iOS platform");
            IOSDriver iosDriver = (IOSDriver) this.driver;
            new CheckboxActions().checkAllEnabledCheckboxes(iosDriver, logger);
            logger.info("Successfully checked all the checkboxes in the current page on iOS platform");
            setSuccessMessage("Successfully checked all the checkboxes in the current page.");
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while checking all the checkboxes in the current page: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in CheckAllCheckboxes iOS action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

