package com.testsigma.addons.network_operations.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import org.openqa.selenium.NoSuchElementException;


@Action(actionText = "Enable WiFi if not enabled",
        applicationType = ApplicationType.ANDROID,
        description = "Enables wifi in your mobile if not enabled")
public class EnableWiFiIfNotEnabledNLP extends AndroidAction {

    private static final String SUCCESS_MESSAGE = "Successfully WiFi is Enabled";
    private static final String WIFI_NOT_ENABLED = "Unable to enable the WIFi";

    @Override
    protected Result execute() throws NoSuchElementException {
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        ConnectionState state = androidDriver.getConnection();
        try {
            if (!state.isWiFiEnabled()) {
                androidDriver.toggleWifi();
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("WiFi Already Enabled!");
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            setErrorMessage(WIFI_NOT_ENABLED);
            return Result.FAILED;
        }
    }
}