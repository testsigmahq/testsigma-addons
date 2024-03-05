package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;

@Action(actionText = "Mark the biometric authentication as test-data",
        description = "We can mark the biometric authentication as pass or fail",
        applicationType = ApplicationType.IOS)
public class BiometricAutomator extends IOSAction {

  @TestData(reference = "test-data",allowedValues = {"PASS","FAIL"})
  private com.testsigma.sdk.TestData testData;

  private boolean isLambdaTest = false;

  @Override
  protected Result execute() throws NoSuchElementException {
    logger.info("Initiating biometric automation...");
    Result result = Result.SUCCESS;
    AppiumDriver appiumDriver = (AppiumDriver) driver;
    String host = appiumDriver.getRemoteAddress().getHost();
    logger.info("Host url: " + host);
    try {
      if (host.contains("lambdatest")) {
        this.isLambdaTest = true;
        processBiometric("lambda-biometric-injection=pass", "lambda-biometric-injection=fail");
      } else if (host.contains("browserstack")) {
        processBiometric( "browserstack_executor: {\"action\":\"biometric\", \"arguments\": {\"biometricMatch\" : \"pass\"}}", "browserstack_executor: {\"action\":\"biometric\", \"arguments\": {\"biometricMatch\" : \"fail\"}}");
      } else if (host.contains("saucelabs")) {
        processBiometric( "sauce:biometrics-authenticate=true", "sauce:biometrics-authenticate=false");
      } else {
        setErrorMessage("Unsupported lab. Biometric authentication supports in Browserstack, lambda-test and sauce-labs only");
        result = Result.FAILED;
      }
    } catch (WebDriverException e) {
      logger.info("Exception occurred biometric authentication not initiated: " + ExceptionUtils.getStackTrace(e));
      result = Result.FAILED;
      setErrorMessage("Biometric authentication not initiated");
    } catch (Exception e) {
      logger.info("Exception occurred : " + ExceptionUtils.getStackTrace(e));
      result = Result.FAILED;
      setErrorMessage("Unable to perform the biometric authentication");
    }
    return result;
  }

  private boolean failScriptRunner(String failScript) throws Exception {
    AndroidDriver androidDriver = (AndroidDriver) driver;
    try {
      Object result = androidDriver.executeScript(failScript);
      if (result != null) {
        logger.info("Script execution result: " + result);
      }
      return true;
    } catch (WebDriverException e) {
      logger.info("Webdriver exception occurred and handled it");
      return false;
    } catch (Exception e) {
      logger.info("Unknown exception in the occurred failScriptRunner: " + e.getMessage());
      throw new Exception(e.getMessage());
    }
  }

  private void processBiometric(String passScript, String failScript) throws Exception {
    AndroidDriver androidDriver = (AndroidDriver) driver;
    if (testData.getValue().toString().equals("PASS")) {
      logger.info("Making the biometric to pass..");
      Object result = androidDriver.executeScript(passScript);
      if (result != null) {
        logger.info("Script execution result: " + result.toString());
        if (result.toString().contains("BROWSERSTACK_COMMAND_EXECUTION_FAILED")) {
          throw new WebDriverException("Biometric auth not initiated");
        }

      }
      logger.info("Biometric passed successfully");
      setSuccessMessage("Successfully marked the biometric to PASS");
    } else {
      logger.info("Making the biometric to fail");
      if (isLambdaTest) {
        boolean firstTry = failScriptRunner(failScript);
        if (!firstTry) {
          throw new WebDriverException("Popup not shown");
        } else {
          failScriptRunner(failScript);
        }
      } else {
        Object result = androidDriver.executeScript(failScript);
        if(result != null) {
          logger.info("Script execution result: " + result);
          if (result.toString().contains("BROWSERSTACK_COMMAND_EXECUTION_FAILED")) {
            throw new WebDriverException("Biometric auth not initiated");
          }
        }

      }
      logger.info("Biometric failed successfully");
      setSuccessMessage("Successfully marked the biometric to FAIL");
    }
  }
}