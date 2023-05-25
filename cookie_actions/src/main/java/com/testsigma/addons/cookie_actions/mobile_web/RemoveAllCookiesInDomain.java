package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove all cookies in domain",
        description = "Deletes a given cookie from the current domain",
        applicationType = ApplicationType.MOBILE_WEB)
public class RemoveAllCookiesInDomain extends com.testsigma.addons.cookie_actions.web.RemoveAllCookiesInDomain {
    @TestData(reference = "domain")
    private com.testsigma.sdk.TestData domain;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setDomain(domain);
        return super.execute();
    }
}


