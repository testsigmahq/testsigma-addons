package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;

@Action(actionText = "Maximize the current window",
        description = "Maximizes the current window",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class MaximizeCurrentWindow extends WebAction {

    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            logger.info("Maximizing current window");
            driver.manage().window().maximize();
            logger.info("Current window maximized");
        } catch (Exception e) {
            logger.info("Maximizing current window failed");
            setErrorMessage("Maximizing current window failed");
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
