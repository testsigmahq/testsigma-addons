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
@Action(actionText = "Verify that the value of key in local storage is test-data",
        description = "Verifies that the key/value pair stored in the local browser storage matches a given key/value pair and returns the status",
        applicationType = ApplicationType.WEB)
public class VerifyThatValueOfKeyLocalStorageIsTestData extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Value of the key is as expected";
    private static final String LOCAL_STORAGE_KEY_NOT_FOUND_ERROR_MESSAGE = "The Local Storage key not found";
    private static final String LOCAL_STORAGE_KEY_VALUE_IS_DIFFERENT_ERROR_MESSAGE = "The Value of the key is different";
    @TestData(reference = "key")
    private com.testsigma.sdk.TestData key;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData expectedValue;

    @Override
    public Result execute() throws NoSuchElementException {
        RemoteExecuteMethod executeMethod = new RemoteExecuteMethod((RemoteWebDriver) driver);
        RemoteWebStorage webStorage = new RemoteWebStorage(executeMethod);
        LocalStorage localStorage = webStorage.getLocalStorage();
        try {
            String actualValue = localStorage.getItem(key.getValue().toString());
            if (actualValue.equals(expectedValue.getValue())) {
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                log("Expected Value - " + expectedValue.getValue().toString() + ", Actual Value - " + actualValue);
                setErrorMessage(LOCAL_STORAGE_KEY_VALUE_IS_DIFFERENT_ERROR_MESSAGE);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            setErrorMessage(LOCAL_STORAGE_KEY_NOT_FOUND_ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

