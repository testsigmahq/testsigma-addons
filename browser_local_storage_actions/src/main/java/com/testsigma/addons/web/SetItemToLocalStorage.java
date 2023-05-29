package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;

@Data
@Action(actionText = "Set key value pair to local storage, Key_Name = Value_Data",
        description = "Action to Set a key value pair to browser's Local Storage.",
        applicationType = ApplicationType.WEB)
public class SetItemToLocalStorage extends WebAction {

    @TestData(reference = "Key_Name")
    private com.testsigma.sdk.TestData keyNameTestData;

    @TestData(reference = "Value_Data")
    private com.testsigma.sdk.TestData valueTestData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        try{
            RemoteExecuteMethod executeMethod = new RemoteExecuteMethod((RemoteWebDriver) driver);
            RemoteWebStorage webStorage = new RemoteWebStorage(executeMethod);
            LocalStorage local = webStorage.getLocalStorage();
            logger.info(String.format("Setting key value pair to Local Storage,<br> key=%s<br>=%s",keyNameTestData.getValue().toString()
            ,valueTestData.getValue().toString()));
            local.setItem(keyNameTestData.getValue().toString(),valueTestData.getValue().toString());
            setSuccessMessage(String.format("Successfully Set key value pair to Local Storage <br>%s=%s",
                    keyNameTestData.getValue().toString(),valueTestData.getValue().toString()));
        }catch (Exception e){
            result = Result.FAILED;
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to set key value pair to Local Storage.");
        }

        return result;
    }
}