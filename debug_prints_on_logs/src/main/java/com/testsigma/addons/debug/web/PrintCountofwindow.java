package com.testsigma.addons.debug.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.util.Set;

@Data
@Action(actionText = "Print count of currently open windows",
    description = "Prints Count of all open windows tab",
    applicationType = ApplicationType.WEB)
public class PrintCountofwindow extends WebAction {

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    logger.info("Initiating execution");

    try {

      Set<String> ls = driver.getWindowHandles();
      int ow = ls.size();
      logger.info("Got Count of Windows" + ow);
      setSuccessMessage("The count of currently opened windows is" + ow);
     

    } catch (Exception e) {
      e.printStackTrace();
      setErrorMessage("Could not print the count of opened window, Check log for more details");
      result = com.testsigma.sdk.Result.FAILED;

    }

    return result;
  }
}
