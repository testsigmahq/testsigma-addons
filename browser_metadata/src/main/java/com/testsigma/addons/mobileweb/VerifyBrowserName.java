package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@Action(
        actionText = "Verify that the browser is test-data",
        description = "Verifies the browser name in mobile web",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false
)
public class VerifyBrowserName extends WebAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating VerifyBrowserName");
        Result result = Result.SUCCESS;

        try {
            AppiumDriver appiumDriver = (AppiumDriver) this.driver;

            String expectedBrowser =
                    testData.getValue().toString().trim().toLowerCase();

            String actualBrowser =
                    ((RemoteWebDriver) appiumDriver).getCapabilities().getBrowserName();

            if (actualBrowser == null || actualBrowser.isEmpty()) {
                logger.warn("Browser name is empty in capabilities");
                setErrorMessage("Browser name is empty in capabilities");
                return Result.FAILED;
            }

            actualBrowser = actualBrowser.trim().toLowerCase();

            logger.info("Expected Browser: " + expectedBrowser);
            logger.info("Actual Browser  : " + actualBrowser);

            if (actualBrowser.equals(expectedBrowser)) {
                logger.info("Browser verification passed. Browser is '" + actualBrowser + "'.");
                setSuccessMessage(
                        "Browser verification passed. Browser is '" + actualBrowser + "'."
                );
            } else {
                logger.warn("Browser verification failed. Expected '" + expectedBrowser +
                        "' but found '" + actualBrowser + "'.");
                setErrorMessage(
                        "Browser verification failed. Expected '" + expectedBrowser +
                                "' but found '" + actualBrowser + "'."
                );
                result =  Result.FAILED;
            }

        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Error while getting browser name::"+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error while getting browser name::" + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}
