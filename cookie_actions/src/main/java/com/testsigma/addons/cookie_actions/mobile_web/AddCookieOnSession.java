package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Add Cookies to the session with Key(Cookie_Name) and Value(Cookie_Value)",
    description = "Add cookies to currently running session when sent as a key and value pair",
    applicationType = ApplicationType.MOBILE_WEB)

public class AddCookieOnSession extends com.testsigma.addons.cookie_actions.web.AddCookieOnSession {


  @TestData(reference = "Key")
  private com.testsigma.sdk.TestData cookieName;

  @TestData(reference = "Value")
  private com.testsigma.sdk.TestData cookieValue;


  @Override
  public Result execute() throws NoSuchElementException {
    super.setCookieName(cookieName);
    super.setCookieValue(cookieValue);
    return super.execute();
  }
}