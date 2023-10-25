package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import java.util.List;

@Data
@Action(actionText = "Get/Fetch value from local storage with key Key_Name and store in a variable Variable_Name",
        description = "Action to fetch a key value pair from browsers Local Storage and save the value to a runtime variable.",
        applicationType = ApplicationType.WEB)
public class GetItemFromLocalStorage extends WebAction {

  @TestData(reference = "Key_Name")
  private com.testsigma.sdk.TestData keyNameTestData;

  @TestData(reference = "Variable_Name",isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableNameTestData;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

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
      logger.info("Setting local storage value to runtime variable:"+variableNameTestData.getValue().toString());
      runTimeData = new com.testsigma.sdk.RunTimeData();
      runTimeData.setValue(value);
      runTimeData.setKey(variableNameTestData.getValue().toString());
      setSuccessMessage(String.format("Successfully fetched value from local storage and set to runtime variable <br>%s=%s",
              variableNameTestData.getValue().toString(),value));
    }catch (Exception e){
      result = Result.FAILED;
      logger.info(ExceptionUtils.getStackTrace(e));
      setErrorMessage("Unable to fetch the local storage value. Please verify if the key is correct.");
    }

    return result;
  }
}