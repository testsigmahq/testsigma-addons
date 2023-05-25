package com.testsigma.addons.local_storage.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Remove key from local storage",
        description = "Removes the key/value pairs from the local browser storage",
        applicationType = ApplicationType.MOBILE_WEB)
public class RemoveKeyFromLocalStorage extends com.testsigma.addons.local_storage.web.RemoveKeyFromLocalStorage {
    @TestData(reference = "key")
    private com.testsigma.sdk.TestData key;

    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}