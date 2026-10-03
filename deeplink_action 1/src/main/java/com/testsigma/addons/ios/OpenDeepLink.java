package com.testsigma.addons.ios;

import com.google.common.collect.ImmutableMap;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.Map;


@Action(actionText = "Open the deep link test-data",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class OpenDeepLink extends IOSAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    protected Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        IOSDriver iosDriver = (IOSDriver) this.driver;

        String deepLink = testData.getValue().toString();
        logger.info("Opening deep link: " + deepLink);

        Object bundleId = iosDriver.getCapabilities().getCapability("appium:bundleId");
        if (!deepLink.contains("://")) {
            // No scheme found, construct using appIdentifier as custom URL scheme
            logger.debug(String.format("Deep link missing scheme, constructing URL " +
                    "with appIdentifier: {}://{}", bundleId, deepLink));
            setErrorMessage(String.format("Deep link missing scheme, constructing URL " +
                    "with appIdentifier: {}://{}", bundleId, deepLink));

        }

        try {
            iosDriver.executeScript("mobile: deepLink", ImmutableMap.of(
                    "url", deepLink,
                    "bundleId", bundleId
            ));
            logger.info("Deep link opened successfully: " + deepLink + " for bundleId: " + bundleId);
        } catch (Exception e) {
            logger.debug("Error while opening deep link: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(String.format("Failed to open deep link %s for package %s. Error: %s .try with this " +
                            "package %s",
                    deepLink, bundleId, e.getMessage()));
            return Result.FAILED;
        }

        setSuccessMessage(String.format("Successfully opened deep link %s for package %s",
                deepLink, bundleId));
        return result;
    }

}
