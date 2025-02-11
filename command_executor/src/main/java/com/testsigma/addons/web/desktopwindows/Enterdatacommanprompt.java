package com.testsigma.addons.web.desktopwindows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

@Data
@Action(actionText = "Execute testdata in commandprompt using robot class",
        description = "it will execute the data in command prompt",
        applicationType = ApplicationType.WINDOWS)
public class Enterdatacommanprompt extends WindowsAction {
	
	@TestData(reference = "testdata")
	  private com.testsigma.sdk.TestData testData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
    	Robot robot = new Robot();

    	StringSelection stringSelection = new StringSelection(testData.getValue().toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);   
        Thread.sleep(3000);     
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    } catch (Exception e) {
		String errorMessage = ExceptionUtils.getStackTrace(e);
		result = com.testsigma.sdk.Result.FAILED;
		setErrorMessage(errorMessage);
		logger.warn(errorMessage);	
	}
    setSuccessMessage("CommandPrompt is opened successfully");
    return result;
  }
}