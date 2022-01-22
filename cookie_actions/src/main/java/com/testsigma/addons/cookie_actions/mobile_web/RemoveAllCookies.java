package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove all cookies",
        description = "Deletes all cookies saved in your browser",
        applicationType = ApplicationType.MOBILE_WEB)
public class RemoveAllCookies extends com.testsigma.addons.cookie_actions.web.RemoveAllCookies {
    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

