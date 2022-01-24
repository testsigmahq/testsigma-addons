package com.testsigma.addons.cookie_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove all cookies",
        description = "Deletes all cookies saved in your browser",
        applicationType = ApplicationType.WEB)
public class RemoveAllCookies extends WebAction {

    private static final String SUCCESS_MESSAGE = "Removed all cookies";

    @Override
    public Result execute() throws NoSuchElementException {
        log("Deleted Cookies - " + driver.manage().getCookies());
        System.out.println("Deleted Cookies - " + driver.manage().getCookies());
        driver.manage().deleteAllCookies();
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}

