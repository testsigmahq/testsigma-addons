package com.testsigma.addons.web.desktopwindows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.event.KeyEvent;

@Data
@Action(actionText = "Close the commandprompt using robot class",
        description = "It will closes the commandprompt",
        applicationType = ApplicationType.WINDOWS)
public class Closecommanprompt extends WindowsAction {

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
    	Robot robot = new Robot();

    	robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_F4);
        robot.keyRelease(KeyEvent.VK_F4);
        robot.keyRelease(KeyEvent.VK_ALT);
        Thread.sleep(2000);
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