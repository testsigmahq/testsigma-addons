package com.testsigma.addons.local_storage.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Clear local storage",
        description = "The local storage stores data with no expiration date. They are stored as key/value pairs. The data will not be deleted when the browser is closed. This action clears the local browser storage.",
        applicationType = ApplicationType.MOBILE_WEB)
public class ClearLocalStorage extends com.testsigma.addons.local_storage.web.ClearLocalStorage {
    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}