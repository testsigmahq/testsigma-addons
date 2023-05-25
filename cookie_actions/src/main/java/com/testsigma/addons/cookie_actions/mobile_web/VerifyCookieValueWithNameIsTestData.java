package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the cookie value with name cookie-name is cookie-value",
        description = "This action checks if the cookie exists.",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyCookieValueWithNameIsTestData extends com.testsigma.addons.cookie_actions.web.VerifyCookieValueWithNameIsTestData {
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "cookie-value")
    private com.testsigma.sdk.TestData cookieValue;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setCookieName(cookieName);
        super.setCookieValue(cookieValue);
        return super.execute();
    }
}

