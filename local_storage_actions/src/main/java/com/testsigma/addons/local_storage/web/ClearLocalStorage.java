package com.testsigma.addons.local_storage.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;

@Data
@Action(actionText = "Clear local storage",
        description = "The local storage stores data with no expiration date. They are stored as key/value pairs. The data will not be deleted when the browser is closed. This action clears the local browser storage.",
        applicationType = ApplicationType.WEB)
public class ClearLocalStorage extends WebAction {

    private static final String SUCCESS_MESSAGE = "Cleared local storage Successfully";

    @Override
    public Result execute() throws NoSuchElementException {
        RemoteExecuteMethod executeMethod = new RemoteExecuteMethod((RemoteWebDriver) driver);
        RemoteWebStorage webStorage = new RemoteWebStorage(executeMethod);
        LocalStorage storage = webStorage.getLocalStorage();
        storage.clear();
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}

