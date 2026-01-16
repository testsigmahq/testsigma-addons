package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

@Data
@Action(
        actionText = "Verify if window size is options",
        description = "Verifies whether the browser window is Maximized or Minimized (Windowed)",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class VerifyWindowSizeIsMaximizedOrMinimizes extends WebAction {

  @TestData(reference = "options",allowedValues = {"Maximized", "Minimized"})
  private com.testsigma.sdk.TestData expectedState;

  @Override
  public Result execute() {
    String expected = expectedState.getValue().toString();
    logger.info("Initiating window size verification. Expecting state: " + expected);

    try {
      boolean isMaximized = isWindowMaximized(driver);

      if (expected.equalsIgnoreCase("Maximized")) {
        if (isMaximized) {
          logger.info("Pass: Window is Maximized as expected.");
          setSuccessMessage("Browser window is Maximized.");
          return Result.SUCCESS;
        } else {
          logger.warn("Fail: Expected Maximized, but window is Minimized/Windowed.");
          setErrorMessage("Browser window is NOT Maximized.");
          return Result.FAILED;
        }
      } else {
        if (!isMaximized) {
          logger.info("Pass: Window is Minimized/Windowed as expected.");
          setSuccessMessage("Browser window is Minimized (Windowed mode).");
          return Result.SUCCESS;
        } else {
          logger.warn("Fail: Expected Minimized, but window is Maximized.");
          setErrorMessage("Browser window is Maximized, expected it to be Minimized.");
          return Result.FAILED;
        }
      }

    } catch (Exception e) {
      logger.info("Failed to verify window state. Error: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to verify window state. Error: " + ExceptionUtils.getMessage(e));
      return Result.FAILED;
    }
  }

  private boolean isWindowMaximized(WebDriver driver) {
    JavascriptExecutor jse = (JavascriptExecutor) driver;

    // We use outerWidth/Height to get the full window size including UI borders
    Long outerWidth = (Long) jse.executeScript("return window.outerWidth;");
    Long outerHeight = (Long) jse.executeScript("return window.outerHeight;");

    // We use screen.availWidth/Height to get the monitor size minus the OS taskbar
    Long availWidth = (Long) jse.executeScript("return screen.availWidth;");
    Long availHeight = (Long) jse.executeScript("return screen.availHeight;");

    logger.info("Window Outer Size : " + outerWidth + " x " + outerHeight);
    logger.info("Screen Avail Size : " + availWidth + " x " + availHeight);

    // Tolerance handles OS-specific invisible borders (shadows) that might make
    // the window report 5-10px smaller than the screen even when maximized.
    boolean widthMaximized = outerWidth >= (availWidth - 15);
    boolean heightMaximized = outerHeight >= (availHeight - 15);

    return widthMaximized && heightMaximized;
  }
}