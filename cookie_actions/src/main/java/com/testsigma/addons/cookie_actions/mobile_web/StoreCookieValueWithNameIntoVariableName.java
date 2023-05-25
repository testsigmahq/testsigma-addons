package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Store cookie value cookie-value into name cookie-name",
        description = "Stores the value of a given cookie name to a variable",
        applicationType = ApplicationType.MOBILE_WEB)
public class StoreCookieValueWithNameIntoVariableName extends com.testsigma.addons.cookie_actions.web.StoreCookieValueWithNameIntoVariableName {
    @TestData(reference = "cookie-value")
    private com.testsigma.sdk.TestData cookieValue;
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

