package com.testsigma.addons.desktopWindows;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.event.KeyEvent;

@Data
@Action(actionText = "Take screenshot using Windows+Alt+PrintScreen",
        description = "Takes a screenshot using the Windows+Alt+PrintScreen key combination and copies it to the clipboard.",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = false)
public class TakeScreenshotWindowsAltPrintScreen extends WindowsAction {

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    // Your Awesome code starts here
    logger.info("Initiating screenshot capture with Windows+Alt+PrintScreen");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
      Robot robot = new Robot();
      robot.delay(100);

      // Press Windows key
      robot.keyPress(KeyEvent.VK_WINDOWS);
      robot.delay(50);
      // Press Alt key
      robot.keyPress(KeyEvent.VK_ALT);
      robot.delay(50);
      // Press PrintScreen key
      robot.keyPress(KeyEvent.VK_PRINTSCREEN);
      robot.delay(50);

      // Release PrintScreen key
      robot.keyRelease(KeyEvent.VK_PRINTSCREEN);
      robot.delay(50);

      // Release Alt key
      robot.keyRelease(KeyEvent.VK_ALT);
      robot.delay(50);

      // Release Windows key
      robot.keyRelease(KeyEvent.VK_WINDOWS);
      robot.delay(2000);

      setSuccessMessage("Successfully took screenshot and copied to clipboard.");

    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info("Unable to take screenshot: " + ExceptionUtils.getStackTrace(error));
      setErrorMessage("Unable to take screenshot: " + error.getMessage());
    }
    return result;
  }
}