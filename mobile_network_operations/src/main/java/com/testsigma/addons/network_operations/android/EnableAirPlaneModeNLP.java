package com.testsigma.addons.network_operations.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "Enable Airplane Mode",
        applicationType = ApplicationType.ANDROID,
        description = "Enables Airplane mode in your mobile")
public class EnableAirPlaneModeNLP extends AndroidAction {

    private static final String SUCCESS_MESSAGE = "Successfully Airplane mode is Enabled";
    private static final String AIRPLANE_MODE_NOT_ENABLED = "Unable to enable the Airplane Mode";

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        ConnectionState state = androidDriver.getConnection();
        try {
            if (!state.isAirplaneModeEnabled()) {
                androidDriver.toggleAirplaneMode();
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("Airplane Mode Already Enabled!");
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            state = androidDriver.getConnection();
            if(state.isAirplaneModeEnabled()){
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            }else{
                setErrorMessage(AIRPLANE_MODE_NOT_ENABLED);
                return Result.FAILED;
            }

        }
    }
}