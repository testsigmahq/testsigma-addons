package com.testsigma.addons.network_operations.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "Enable Cellular Data if not Enabled",
        applicationType = ApplicationType.ANDROID,
        description = "Enables Cellular data in your mobile")
public class EnableCellularDataIfNotEnabledNLP extends AndroidAction {

    private static final String SUCCESS_MESSAGE = "Successfully Cellular Data is Enabled";
    private static final String CELLULAR_DATA_NOT_ENABLED = "Unable to enable the Cellular Data";

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        ConnectionState state = androidDriver.getConnection();
        try {
            if (!state.isDataEnabled()) {
                androidDriver.toggleData();
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("Cellular Data is Already Enabled!");
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            setErrorMessage(CELLULAR_DATA_NOT_ENABLED);
            return Result.FAILED;
        }
    }
}