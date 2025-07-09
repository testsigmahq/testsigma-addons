package com.testsigma.addons.android;

import com.google.common.collect.ImmutableMap;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;


@Action(actionText = "open the deep link test-data for the package package-name",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class OpenDeepLink extends AndroidAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "package-name")
    private com.testsigma.sdk.TestData packageName;

    @Override
    protected Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        AndroidDriver androidDriver = (AndroidDriver) this.driver;

        String deepLink = testData.getValue().toString();
        String packageNameValue = packageName.getValue().toString();
        logger.info("Opening deep link: " + deepLink + " for package: " + packageNameValue);
        String currentPackage = androidDriver.getCurrentPackage();
        logger.info("Current package: " + currentPackage);

        try {
            androidDriver.executeScript("mobile: deepLink", ImmutableMap.of(
                "url", deepLink,
                "package", packageNameValue
            ));
            logger.info("Deep link opened successfully: " + deepLink + " for package: " + packageNameValue);
        } catch (Exception e) {
            logger.debug("Error while opening deep link: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(String.format("Failed to open deep link %s for package %s. Error: %s .try with this " +
                            "package %s",
                    deepLink, packageNameValue, e.getMessage(), currentPackage));
            return Result.FAILED;
        }

        setSuccessMessage(String.format("Successfully opened deep link %s for package %s",
                deepLink, packageNameValue));
        return result;
    }

}
