package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;

@Data
@Action(actionText = "Verify execution mode is options",
        description = "Verifies whether the browser is running in headed or headless mode",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyHeadlessOrHeadedModeAction extends WebAction {

    @TestData(reference="options", allowedValues = {"Headless", "Headed"})
    private com.testsigma.sdk.TestData mode;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Checking if browser is running in headless mode");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String userAgent = (String) js.executeScript("return navigator.userAgent;");
            logger.info("User Agent: " + userAgent);

            if (userAgent.contains("Firefox")) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("This action is not supported for Firefox browser.");
                return result;
            }

            if ("headless".equalsIgnoreCase(mode.getValue().toString())) {
                if (userAgent.contains("Headless")) {
                    logger.info("Browser is running in HEADLESS mode");
                    setSuccessMessage("Browser is running in HEADLESS mode");
                } else {
                    result = com.testsigma.sdk.Result.FAILED;
                    logger.warn("Browser is running in HEADED mode");
                    setErrorMessage("Browser is running in HEADED mode");
                    return result;
                }
            } else {
                if (!userAgent.contains("Headless")) {
                    logger.info("Browser is running in HEADED mode");
                    setSuccessMessage("Browser is running in HEADED mode");
                } else {
                    result = com.testsigma.sdk.Result.FAILED;
                    logger.warn("Browser is running in HEADLESS mode");
                    setErrorMessage("Browser is running in HEADLESS mode");
                    return result;
                }
            }

        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Error checking headless mode: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to determine browser mode: " + ExceptionUtils.getMessage(e));
        }
        return result;
    }
}