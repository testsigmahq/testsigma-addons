package com.testsigma.addons.debug.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Print current window title",
    description = "Prints Current Window Title",
    applicationType = ApplicationType.WEB)
public class PrintWindowTitle extends WebAction {


  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");

    try {

      String windows_title = driver.getTitle();
      logger.info(windows_title);
      setSuccessMessage("Name of current windows title is " + windows_title);

    } catch (Exception e) {
      e.printStackTrace();
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("Could not Print Window title,Check log for more detail ");
      logger.debug(e.getMessage() + e.getMessage());

    }

    return result;
  }
}
