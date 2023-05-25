package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify cookie with cookie-name exists",
        description = "Verifies that a given value for a given cookie name matches",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyCookieWithNameExists extends com.testsigma.addons.cookie_actions.web.VerifyCookieWithNameExists {
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setCookieName(cookieName);
        return super.execute();
    }
}

