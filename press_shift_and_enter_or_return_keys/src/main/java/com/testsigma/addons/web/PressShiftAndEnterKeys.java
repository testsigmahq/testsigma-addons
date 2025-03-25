package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

@Data
@Action(actionText = "Press the Shift + Enter/Return Keys",
        description = "Simulates pressing Shift + Enter/Return keys on the page.",
        applicationType = ApplicationType.WEB)
public class PressShiftAndEnterKeys extends WebAction {

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            Actions actions = new Actions(driver);

            // Simulate Shift + Enter key press *on the whole page*
            actions.keyDown(Keys.SHIFT)
                    .sendKeys(Keys.ENTER)
                    .keyUp(Keys.SHIFT)
                    .perform();

            setSuccessMessage("Successfully pressed the Shift+Enter keys.");
        } catch (Exception e) {
            logger.warn("Error pressing Shift+Enter: " +  ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;  // Set result to FAILED
            setErrorMessage("Failed to press Shift+Enter: " + ExceptionUtils.getStackTrace(e));
        }

        return result;
    }
}