package com.justdial.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Print Page Source in log",
        description = "Print Page Source in log",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class PrintPageSource extends WebAction {


    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            logger.info("Printing page source");
            logger.info(driver.getPageSource());
            setSuccessMessage("Successfully printed page source to log. You can verify the log file under additional NLP Logs");
            return result;

        } catch (Exception error) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info(" Unable to print page source:" + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Unable to print page source:" + error.getMessage());
            return result;
        }

    }
}