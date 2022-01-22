package com.testsigma.addons.developer_console_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify there is error message with text containing test-data",
        description = "Verifies if a given error is found in the developer console",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyThereIsErrorMessageWithTextContainingTestData extends com.testsigma.addons.developer_console_actions.web.VerifyThereIsErrorMessageWithTextContainingTestData {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

