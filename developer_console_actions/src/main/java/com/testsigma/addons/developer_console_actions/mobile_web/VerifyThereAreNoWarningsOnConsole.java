package com.testsigma.addons.developer_console_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify there are no warnings on console",
        description = "Diagnose if there are any browser dev console warnings, and if there is, return the status",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyThereAreNoWarningsOnConsole extends com.testsigma.addons.developer_console_actions.web.VerifyThereAreNoWarningsOnConsole {
    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

