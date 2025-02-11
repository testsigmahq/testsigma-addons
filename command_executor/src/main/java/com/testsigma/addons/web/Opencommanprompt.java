package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;

@Data
@Action(actionText = "Open the commandprompt using robot class",
        description = "It will openes the commandprompt",
        applicationType = ApplicationType.WEB)
public class Opencommanprompt extends WebAction {

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
    	Robot robot = new Robot();

		robot.keyPress(KeyEvent.VK_WINDOWS);
		robot.keyPress(KeyEvent.VK_R);
		robot.keyRelease(KeyEvent.VK_R);
		robot.keyRelease(KeyEvent.VK_WINDOWS);
		Thread.sleep(1000);
		String cmdPath = "cmd";
		for (char c : cmdPath.toCharArray()) {
			robot.keyPress(Character.toUpperCase(c));
			robot.keyRelease(Character.toUpperCase(c));
		}
		robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
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