package com.testsigma.addons.ios;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute JavaScript snippet testdata and store the returned value in variable var1",
        description = "Executes JS and stores the value in runtime variable",
        applicationType = ApplicationType.IOS)
public class StoreJsValue extends IOSAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData jssnippet;
  
  @TestData(reference = "var1", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variable;
 
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    logger.info("JS SNIPPET IS "+jssnippet.getValue().toString());

    Result result = Result.SUCCESS;
    try {
        IOSDriver iosDriver = (IOSDriver) driver;
        JavascriptExecutor js = (JavascriptExecutor) iosDriver;
        Object obj = js.executeScript(jssnippet.getValue().toString());
        String str = (obj == null) ? "" : obj.toString();

        runTimeData.setKey(variable.getValue().toString());
        runTimeData.setValue(str);
        setSuccessMessage("Successfully stored retured result "+str+" into a runtime variable "+runTimeData);
    }
    catch(Exception e) {
        result = Result.FAILED;
        logger.debug("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
        setErrorMessage("Failed to perform operation " + ExceptionUtils.getMessage(e));
    }
    
     return result;
  }
}