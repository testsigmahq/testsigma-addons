package com.testsigma.addons.local_storage.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Add value for key in local storage",
        description = "Sets key/value pairs to the local browser storage",
        applicationType = ApplicationType.MOBILE_WEB)
public class AddValueForKeyLocalStorage extends com.testsigma.addons.local_storage.web.AddValueForKeyLocalStorage {
    @TestData(reference = "key")
    private com.testsigma.sdk.TestData key;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}