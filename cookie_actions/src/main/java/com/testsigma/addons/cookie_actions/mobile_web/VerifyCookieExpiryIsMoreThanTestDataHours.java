package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify cookie cookie-name expiry is more than number-of-hours hours",
        description = "Verifies if the cookie with a given name expires only after a given hour(s)",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyCookieExpiryIsMoreThanTestDataHours extends com.testsigma.addons.cookie_actions.web.VerifyCookieExpiryIsMoreThanTestDataHours {
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "number-of-hours")
    private com.testsigma.sdk.TestData numberOfHours;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

