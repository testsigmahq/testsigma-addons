package com.testsigma.addons.local_storage.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;

@Data
@Action(actionText = "Remove key from local storage",
        description = "Removes the key/value pairs from the local browser storage",
        applicationType = ApplicationType.WEB)
public class RemoveKeyFromLocalStorage extends WebAction {

    private static final String SUCCESS_MESSAGE = "Remove key from local storage successfully";
    private static final String ERROR_MESSAGE = "The Given key not found in the local storage successfully";

    @TestData(reference = "key")
    private com.testsigma.sdk.TestData key;

    @Override
    public Result execute() throws NoSuchElementException {
        RemoteExecuteMethod executeMethod = new RemoteExecuteMethod((RemoteWebDriver) driver);
        RemoteWebStorage webStorage = new RemoteWebStorage(executeMethod);
        LocalStorage localStorage = webStorage.getLocalStorage();
        try {
            localStorage.removeItem(key.getValue().toString());
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        } catch (Exception exception) {
            setErrorMessage(ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

