package com.testsigma.addons.mobileWeb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebDriverException;

@Data
@Action(actionText = "Switch to app using package-name",
        description = "Switches to an app using its package name (Android) or bundle ID (iOS).",
        applicationType = ApplicationType.MOBILE_WEB)
public class SwitchtoAppUsingPackageName extends WebAction {

  @TestData(reference = "package-name")
  private com.testsigma.sdk.TestData packageName;

  @Override
  public Result execute() {
    Result result = Result.SUCCESS;
    AppiumDriver driver1 = (AppiumDriver) driver;
    String targetAppIdentifier = packageName.getValue().toString();

    logger.info("Switching to app: Identifier - " + targetAppIdentifier);

    try {
      // Switch to the specified app
      switchToApp(driver1, targetAppIdentifier);
      setSuccessMessage("Successfully switched to app with identifier: " + targetAppIdentifier);
    } catch (Exception e) {
      logger.warn("Error switching to app: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to switch to app with identifier: " + targetAppIdentifier + ". Error: " + ExceptionUtils.getStackTrace(e));
      result = Result.FAILED;
    }

    return result;
  }

  private void switchToApp(AppiumDriver driver, String appIdentifier) {
    try {
      if (driver instanceof AndroidDriver) {
        logger.info("Android driver found. Switching to app: " + appIdentifier);
        ((AndroidDriver) driver).activateApp(appIdentifier);
      } else if (driver instanceof IOSDriver) {
        logger.info("IOS driver found. Switching to app: " + appIdentifier);
        ((IOSDriver) driver).activateApp(appIdentifier);
      } else {
        throw new WebDriverException("Unsupported driver type.  This action supports AndroidDriver and IOSDriver.");
      }
      logger.info("Successfully activated app " + appIdentifier);
    } catch (Exception e) {
      logger.warn("Error activating app: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to activate app: " + ExceptionUtils.getStackTrace(e));
      throw new WebDriverException("Failed to activate app: " + ExceptionUtils.getStackTrace(e));
    }
  }
}