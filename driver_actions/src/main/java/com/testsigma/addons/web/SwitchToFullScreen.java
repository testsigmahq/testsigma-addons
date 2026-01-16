package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;

@Action(actionText = "Switch to FullScreen",
        description = "Opens Current window in FullScreen",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SwitchToFullScreen extends WebAction {
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            logger.info("Opening Current window in FullScreen");
            driver.manage().window().fullscreen();
            logger.info("Opened Current window in FullScreen");
        } catch (Exception e) {
            logger.info("Opening Current window in FullScreen failed");
            setErrorMessage("Opening Current window in FullScreen failed");
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
