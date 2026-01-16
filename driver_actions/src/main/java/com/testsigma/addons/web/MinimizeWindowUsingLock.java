package com.testsigma.addons.web;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Minimize the current window for testdata seconds",
        description = "Minimizes the current window for the specified seconds",
        applicationType = ApplicationType.WEB, useCustomScreenshot = false)
public class MinimizeWindowUsingLock extends WebAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;

    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            int waitInSeconds = Integer.parseInt(testData.getValue().toString());
            logger.info("minimizing window");
            driver.manage().window().minimize();
            if(waitInSeconds < 0) {
                result = com.testsigma.sdk.Result.FAILED;
                logger.info("Wait Duration can't be less than 0");
                setErrorMessage("Wait Duration can't be less than 0");
                return result;
            } else if (waitInSeconds > 60) {
                // setting maximum wait time to 60 seconds
                waitInSeconds = 60;
            }
            this.restStepWait(waitInSeconds);
            logger.info("minimized window");
            setSuccessMessage("Successfully minimized window");
        } catch (NumberFormatException e) {
            logger.info("Please enter a valid number");
            setErrorMessage("Please enter a valid number");
            result = com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to minimize window. Error: " + ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }

    private void restStepWait(Integer waitInSeconds) {
        synchronized (this) {
            try {
                this.wait((waitInSeconds * 1000) - 10);
            } catch (Exception e) {
                logger.info(ExceptionUtils.getStackTrace(e));
                setErrorMessage("Unable to minimize window. Error: " + ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
