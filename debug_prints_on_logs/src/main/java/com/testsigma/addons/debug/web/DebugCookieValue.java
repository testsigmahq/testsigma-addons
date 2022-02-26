package com.testsigma.addons.debug.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Print value of cookies with name test_data",
    description = "Prints the value of cookie with given name",
    applicationType = ApplicationType.WEB)

public class DebugCookieValue extends WebAction {

  com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
  @TestData(reference = "test_data")
  private com.testsigma.sdk.TestData testData;
  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    try {
      Cookie cookieValue = driver.manage().getCookieNamed(testData.getValue().toString());
      //System.out.println(cookieValue.getValue());
      setSuccessMessage("The value of cookie is  " + cookieValue.getValue().toString());

    } catch (Exception e) {

      e.printStackTrace();
      logger.debug(e.getMessage() + e.getCause());
      result = Result.FAILED;
    }

    return result;

  }
}


