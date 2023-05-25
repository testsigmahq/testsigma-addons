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
@Action(actionText = "Verify that the value of Local Storage key Key_Name is equal to Test Data",
        description = "Action to Verify the value of a Local storage Key",
        applicationType = ApplicationType.WEB)
public class VerifyLocalStorageValue extends WebAction {

    @TestData(reference = "Key_Name")
    private com.testsigma.sdk.TestData keyNameTestData;

    @TestData(reference = "Test Data")
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
            logger.info("Fetching local storage value for key:"+keyNameTestData.getValue().toString());
            String value = local.getItem(keyNameTestData.getValue().toString());
            logger.info("Fetched value for given key:"+value);
            if(valueTestData.getValue().toString().equals(value)){
                logger.info("Fetched value from Local Storage and expected value is matching");
                setSuccessMessage(String.format("Successfully verified that the expected value is matching with the Local storage value.<br>" +
                        "Expected Value=%s <br>Actual Value=%s",valueTestData.getValue().toString(),value));
            }else{
                result = Result.FAILED;
                setErrorMessage(String.format("Local storage value is not matching with the expected value.<br>" +
                        "Expected Value=%s <br>Actual Value=%s",valueTestData.getValue().toString(),value));

            }
        }catch (Exception e){
            result = Result.FAILED;
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to fetch the local storage value. Please verify if the key is correct.");
        }
        return result;
    }
}