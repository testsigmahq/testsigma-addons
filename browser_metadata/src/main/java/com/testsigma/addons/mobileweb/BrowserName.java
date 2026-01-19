package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;

@Data
@Action(actionText = "Store browser name in a variable variable-name",
        description = "Stores the current browser name in a variable",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class BrowserName extends WebAction {

  @TestData(reference = "variable-name",isRuntimeVariable = true)
  private com.testsigma.sdk.TestData testData;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    runTimeData = new com.testsigma.sdk.RunTimeData();
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
        AppiumDriver appiumDriver = (AppiumDriver) this.driver;
        String browserName = ((RemoteWebDriver) appiumDriver).getCapabilities().getBrowserName();
        runTimeData.setKey(testData.getValue().toString());
        runTimeData.setValue(browserName);
        logger.info("Browser name is::"+browserName);
    } catch (Exception e) {
        result = com.testsigma.sdk.Result.FAILED;
        logger.warn("Error while getting browser name::"+ ExceptionUtils.getStackTrace(e));
        setErrorMessage("Error while getting browser name::" + ExceptionUtils.getMessage(e));
    }
    setSuccessMessage("Browser name is stored in variable::"+testData.getValue().toString()+" with value::"+runTimeData.getValue());
    return result;
  }
}
