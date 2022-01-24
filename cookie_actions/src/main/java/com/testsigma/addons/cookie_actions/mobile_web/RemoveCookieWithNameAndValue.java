package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove cookie with name cookie-name and value cookie-value",
        description = "Verifies if cookie with a specific cookie name and value exists and returns status",
        applicationType = ApplicationType.MOBILE_WEB)
public class RemoveCookieWithNameAndValue extends com.testsigma.addons.cookie_actions.web.RemoveCookieWithNameAndValue {
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "cookie-value")
    private com.testsigma.sdk.TestData cookieValue;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

