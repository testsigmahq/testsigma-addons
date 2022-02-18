package com.testsigma.addons.cookie_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Add cookie to the session with cookie_name and cookie_value with expiry_time",
    description = "Add cookies to currently running session when sent as a key and value pair and expiry_time.Expiry time should be in this sample format -Thu, 18 Dec 2023 11:00:00 UTC",
    applicationType = ApplicationType.WEB)

public class AddCookieWithExpiry extends WebAction {


  @TestData(reference = "cookie_name")
  private com.testsigma.sdk.TestData cookieName;

  @TestData(reference = "cookie_value")
  private com.testsigma.sdk.TestData cookieValue;

  @TestData(reference = "expiry_time")
  private com.testsigma.sdk.TestData cookieExpiry;


  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    logger.info("Initiating execution and starting to add cookies");

    try {
      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript("document.cookie = \"" + cookieName.getValue().toString() + "=" + cookieValue.getValue().toString() + "; expires=" + cookieExpiry.getValue().toString() + "; path=/\";\r\n"
          + "");
      logger.info("Success in adding cookies with expiry setting " +
          "status to pass cookie name ::" + cookieName.getValue() +
          " value::" + cookieValue.getValue().toString() +
          " cookieExpiry::" + cookieExpiry.getValue().toString()
      );
    } catch (Exception e) {
      result = Result.FAILED;
      setErrorMessage("Could not add cookies with given key  " + cookieName.getValue().toString() + "  and value   " + cookieValue.getValue().toString());
      logger.debug(e.getMessage() + e.getCause());

    }

    return result;
  }
}