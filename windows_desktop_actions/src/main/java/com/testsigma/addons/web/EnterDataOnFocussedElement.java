package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

@Data
@Action(actionText = "Enter data Test-Data on focussed element",
        description = "Enters data using keyboard on the focussed element",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = false)
public class EnterDataOnFocussedElement extends WindowsAction {

  @TestData(reference = "Test-Data")
  private com.testsigma.sdk.TestData testData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
        String data = testData.getValue().toString();
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable transferable = new StringSelection(data);
      clipboard.setContents(transferable, null);
      Robot robot = new Robot();
      robot.delay(100); // Delay to switch to the application where you want to paste the text

      // Press Ctrl+V (Cmd+V on Mac) to paste
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      setSuccessMessage("Successfully entered given data on focussed element");

    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info("Unable to enter data on focussed element:"+ExceptionUtils.getStackTrace(error));
      setErrorMessage("Unable to enter data on focussed element:"+error.getMessage());
    }
    return result;
  }
}