package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify cookie cookie-name expiry is more than number-of-days days",
        description = "Verifies if the cookie with a given name expires only after a given specific number of day(s)",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyCookieExpiryIsMoreThanTestDataDays extends com.testsigma.addons.cookie_actions.web.VerifyCookieExpiryIsMoreThanTestDataDays {
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "number-of-days")
    private com.testsigma.sdk.TestData numberOfDays;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setCookieName(cookieName);
        super.setNumberOfDays(numberOfDays);
        return super.execute();
    }
}

