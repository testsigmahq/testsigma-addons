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
@Action(actionText = "Store cookie value cookie-value into name cookie-name",
        description = "Stores the value of a given cookie name to a variable",
        applicationType = ApplicationType.WEB)
public class StoreCookieValueWithNameIntoVariableName extends WebAction {

    private static final String SUCCESS_MESSAGE = "Stored the Cookie successfully";
    @TestData(reference = "cookie-value")
    private com.testsigma.sdk.TestData cookieValue;
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;

    @Override
    public Result execute() throws NoSuchElementException {
        Cookie cookie = new Cookie(cookieName.getValue().toString(), cookieValue.getValue().toString());
        driver.manage().addCookie(cookie);
        log("Cookies - " + driver.manage().getCookies());
        System.out.println("Cookies - " + driver.manage().getCookies());
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}

