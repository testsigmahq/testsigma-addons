package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(
  actionText = "Get the current device date time in format dateformat and store into runtime variable variable-name",
  description = "Gets the current device date and time and stores it into a runtime variable",
  applicationType = ApplicationType.IOS,
  useCustomScreenshot = false
)
public class GetCurrentDeviceDateOrTime extends WebAction {

  @TestData(reference = "dateformat")
  private com.testsigma.sdk.TestData dateFormat;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData runtimeVariable;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");
    Result result = Result.SUCCESS;

    try {
      String format = dateFormat.getValue().toString();
      IOSDriver iosDriver = (IOSDriver) this.driver;

      String deviceTime = iosDriver.getDeviceTime(format);
      logger.info("Current device date/time is " + deviceTime);

      runTimeData.setKey(runtimeVariable.getValue().toString());
      runTimeData.setValue(deviceTime);

      setSuccessMessage("Current device date/time is " + deviceTime);

    } catch (Exception e) {
      result = Result.FAILED;
      setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
      logger.warn("Exception Occurred: "  + ExceptionUtils.getStackTrace(e));
    }

    return result;
  }
}