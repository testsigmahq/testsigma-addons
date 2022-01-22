package com.testsigma.addons.network_operations.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.util.Assert;

@Action(actionText = "Verify WiFi is Enabled",
        applicationType = ApplicationType.ANDROID,
        description = "Verifies whether WiFi in your mobile is turned on")
public class WiFiEnabledStatusNLP extends AndroidAction {

    private static final String SUCCESS_MESSAGE = "Successfully verified that the WiFi is Enabled";
    private static final String WIFI_NOT_ENABLED = "The Airplane mode is in Off state";

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        ConnectionState state = androidDriver.getConnection();
        Assert.isTrue(state.isWiFiEnabled(), WIFI_NOT_ENABLED);
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}