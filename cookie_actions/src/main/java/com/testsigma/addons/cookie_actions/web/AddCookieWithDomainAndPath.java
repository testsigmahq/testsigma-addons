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
@Action(actionText = "Add cookie to the session with cookie_name and cookie_value with expiry_time and domain_name and path ",
    description = "Add cookies to currently running session when sent as a key and value pair with expiry_time,domain name and path.Expiry time should be in this sample format -Thu, 18 Dec 2023 11:00:00 UTC",
    applicationType = ApplicationType.WEB)

public class AddCookieWithDomainAndPath extends WebAction {
  @TestData(reference = "cookie_name")
  private com.testsigma.sdk.TestData cookieName;

  @TestData(reference = "cookie_value")
  private com.testsigma.sdk.TestData cookieValue;

  @TestData(reference = "expiry_time")
  private com.testsigma.sdk.TestData cookieExpiry;

  @TestData(reference = "domain_name")
  private com.testsigma.sdk.TestData cookieDomain;
  @TestData(reference = "path")
  private com.testsigma.sdk.TestData cookiePath;


  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    logger.info("Initiating execution and starting to add cookies");

    try {
      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript("document.cookie = \"" + cookieName.getValue().toString() + "=" + cookieValue.getValue().toString() + "; domain=" + cookieDomain.getValue().toString() + "; expires=" + cookieExpiry.getValue().toString() + "; path=" + cookiePath.getValue().toString() + "\";\r\n"
          + "");
      logger.info("Success in adding cookies with expiry setting " +
          "status to pass cookie name ::" + this.cookieName.getValue() +
          " value::" + cookieValue.getValue().toString() +
          " cookieExpiry::" + cookieExpiry.getValue().toString() +
          " cookieDomain::" + cookieDomain.getValue().toString() +
          " cookiePath::" + cookiePath.getValue().toString()
      );
    } catch (Exception e) {
      result = Result.FAILED;
      setErrorMessage("Could not perform action check logger for more details");
      logger.debug(e.getMessage() + e.getCause());
    }

    return result;
  }
}