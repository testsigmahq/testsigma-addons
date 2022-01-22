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
@Action(actionText = "Remove cookie with name cookie-name and value cookie-value",
        description = "Verifies if cookie with a specific cookie name and value exists and returns status",
        applicationType = ApplicationType.WEB)
public class RemoveCookieWithNameAndValue extends WebAction {

    private static final String SUCCESS_MESSAGE = "Removed cookie";
    private static final String NO_COOKIE_WITH_NAME_ERROR_MESSAGE = "Cookie with the name not found";
    private static final String COOKIE_VALUE_NOT_AS_EXPECTED = "Cookie Value Not as Expected";
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "cookie-value")
    private com.testsigma.sdk.TestData cookieValue;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            Cookie cookie = driver.manage().getCookieNamed(cookieName.getValue().toString());
            log("Cookie " + cookieName.getValue() + "'s value: " + cookie.getValue());
            System.out.println("Cookie " + cookieName.getValue() + "'s value: " + cookie.getValue());
            if (cookie.getValue().equals(cookieValue.getValue().toString())) {
                driver.manage().deleteCookie(cookie);
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                setErrorMessage(COOKIE_VALUE_NOT_AS_EXPECTED);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            setErrorMessage(NO_COOKIE_WITH_NAME_ERROR_MESSAGE);
            return Result.FAILED;
        }

    }
}

