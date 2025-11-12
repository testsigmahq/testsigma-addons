
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
@Action(actionText = "Uncheck all the checkboxes in the current page",
        description = "Uncheck all the checkboxes in the current page",
        applicationType = ApplicationType.ANDROID)
public class UnCheckAllCheckboxes extends AndroidAction {

    @Override
    public Result execute() {
        try {
            logger.info("Starting to uncheck all checkboxes on Android platform");
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            new CheckboxActions().unCheckAllEnabledCheckboxes(androidDriver, logger);
            logger.info("Successfully unchecked all the checkboxes in the current page on Android platform");
            setSuccessMessage("Successfully unchecked all the checkboxes in the current page.");
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while unchecking all the checkboxes in the current page: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in UnCheckAllCheckboxes Android action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

