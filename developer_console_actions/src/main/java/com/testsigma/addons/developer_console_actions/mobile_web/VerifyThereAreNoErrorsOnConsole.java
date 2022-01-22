package com.testsigma.addons.developer_console_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify there are no errors on console",
        description = "Verifies if no errors are found in the developer console and return the status",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyThereAreNoErrorsOnConsole extends com.testsigma.addons.developer_console_actions.web.VerifyThereAreNoErrorsOnConsole {
    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

