package com.testsigma.addons.network_operations.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.util.Assert;

@Action(actionText = "Verify Cellular Data is Enabled",
        applicationType = ApplicationType.ANDROID,
        description = "Verifies whether the cellular data in your mobile is turned on")
public class VerifyCellularDataEnabledNLP extends AndroidAction {
    private static final String SUCCESS_MESSAGE = "Successfully verified that Cellular Data is Enabled";
    private static final String CELLULAR_DATA_NOT_ENABLED = "The Cellular Data is in Off state";

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        ConnectionState state = androidDriver.getConnection();
        Assert.isTrue(state.isDataEnabled(), CELLULAR_DATA_NOT_ENABLED);
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}