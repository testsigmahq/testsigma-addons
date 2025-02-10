package com.testsigma.addons.lambdatest.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;


@Data
@Action(actionText = "simulate network to profile-name mode",
        description = "Simulates the behavior of given network.",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class SimulateNetworks extends AndroidAction {

    @TestData(reference = "profile-name", allowedValues = {"no-network", "default", "2G", "3G", "4G"})
    com.testsigma.sdk.TestData profileName;
    @Override
    protected com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution to simulate network configuration on LambdaTest");

        final String successMessage = "Successfully switched to network-configuration : ";
        final String errorMessage = "Failed to switch to network configuration ";

        // REFERENCE : https://www.lambdatest.com/support/docs/app-auto-network-throttling/

        try {
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            logger.info("profile-name: " + profileName.getValue().toString());
            String networkProfile = getLambdaTestProfileName(profileName.getValue().toString().toLowerCase());
            logger.info("updateNetworkProfile : " + networkProfile);
            androidDriver.executeScript("updateNetworkProfile="+networkProfile);
            setSuccessMessage(successMessage + networkProfile);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.warn("stack trace: " + sw.toString());
            setErrorMessage(errorMessage + e.getMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private String getLambdaTestProfileName(String modifiedProfileName) {
        switch (modifiedProfileName) {
            case "no-network":
                return "offline";
            case "2g":
                return "2g-gprs-good";
            case "3g":
                return "3g-umts-good";
            case "4g":
                return "4g-lte-good";
            default:
                return "default";
        }
    }

}
