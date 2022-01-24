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
@Action(actionText = "Verify that the cookie value with name cookie-name is cookie-value",
        description = "Verifies that a given value for a given cookie name matches",
        applicationType = ApplicationType.WEB)
public class VerifyCookieValueWithNameIsTestData extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Cookie has the expected value";
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
            if (cookie.getValue().equals(cookieValue.getValue().toString())) {
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                log(cookie.getValue());
                setErrorMessage(COOKIE_VALUE_NOT_AS_EXPECTED);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            setErrorMessage(NO_COOKIE_WITH_NAME_ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

