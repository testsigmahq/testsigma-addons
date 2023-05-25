package com.testsigma.addons.debug.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;

import java.util.Set;

@Data
@Action(actionText = "Print all available cookies in the session",
    description = "Prints all available cookies in the session",
    applicationType = ApplicationType.WEB)
public class PrintCookies extends WebAction {


  com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    try {

      Set<Cookie> cookies = driver.manage().getCookies();
      int size = cookies.size();
      setSuccessMessage("No of Cookies available is" + size + "  The following cookies are" + cookies);


    } catch (Exception e) {
      e.printStackTrace();
      setErrorMessage("Operation Failed. Check Logs for more details");
      logger.debug(e.getMessage() + e.getCause());
      result = com.testsigma.sdk.Result.FAILED;

    }


    return result;

  }
}

