
package com.testsigma.addons.android;

import com.testsigma.addons.utils.CheckboxActions;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Check all the checkboxes in the current page",
        description = "Check all the checkboxes in the current page",
        applicationType = ApplicationType.ANDROID)
public class CheckAllCheckboxes extends AndroidAction {

    @Override
    public Result execute() {
        try {
            logger.info("Starting to check all checkboxes on Android platform");
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            new CheckboxActions().checkAllEnabledCheckboxes(androidDriver, logger);
            logger.info("Successfully checked all the checkboxes in the current page on Android platform");
            setSuccessMessage("Successfully checked all the checkboxes in the current page.");
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while checking all the checkboxes in the current page: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in CheckAllCheckboxes Android action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

