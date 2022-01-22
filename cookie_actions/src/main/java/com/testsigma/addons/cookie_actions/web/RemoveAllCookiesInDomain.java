package com.testsigma.addons.cookie_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove all cookies in domain",
        description = "Deletes a given cookie from the current domain",
        applicationType = ApplicationType.WEB)
public class RemoveAllCookiesInDomain extends WebAction {

    private static final String SUCCESS_MESSAGE = "Removed all cookies in the domain";
    private static final String FAILURE_MESSAGE = "No Cookies in the domain";

    @TestData(reference = "domain")
    private com.testsigma.sdk.TestData domain;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            driver.get(domain.getValue().toString());
        }
        catch(Exception exception){
            setSuccessMessage(FAILURE_MESSAGE);
            return Result.FAILED;
        }
        driver.manage().deleteAllCookies();
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}

