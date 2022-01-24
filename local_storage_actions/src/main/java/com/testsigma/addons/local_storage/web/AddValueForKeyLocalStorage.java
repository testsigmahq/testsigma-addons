package com.testsigma.addons.local_storage.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;

@Data
@Action(actionText = "Add value for key in local storage",
        description = "Sets key/value pairs to the local browser storage",
        applicationType = ApplicationType.WEB)
public class AddValueForKeyLocalStorage extends WebAction {

    private static final String SUCCESS_MESSAGE = "Updated the local storage Successfully";
    @TestData(reference = "key")
    private com.testsigma.sdk.TestData key;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    @Override
    public Result execute() throws NoSuchElementException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.setItem(arguments[0],arguments[1])",key.getValue(),value.getValue());
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}

