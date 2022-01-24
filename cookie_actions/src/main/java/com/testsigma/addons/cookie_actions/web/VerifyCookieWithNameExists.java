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
@Action(actionText = "Verify cookie with cookie-name exists",
        description = "This action checks if the cookie exists.",
        applicationType = ApplicationType.WEB)
public class VerifyCookieWithNameExists extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Expected Cookie was found";
    private static final String NO_COOKIE_WITH_NAME_ERROR_MESSAGE = "Cookie with the name not found";
    private static final String COOKIE_VALUE_NOT_AS_EXPECTED = "Cookie Value Not as Expected";
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            Cookie cookie = driver.manage().getCookieNamed(cookieName.getValue().toString());
            if(cookie!=null) {
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                setErrorMessage(NO_COOKIE_WITH_NAME_ERROR_MESSAGE);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            setErrorMessage(NO_COOKIE_WITH_NAME_ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

