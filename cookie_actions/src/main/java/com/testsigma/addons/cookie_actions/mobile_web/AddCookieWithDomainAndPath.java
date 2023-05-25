package com.testsigma.addons.cookie_actions.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Add cookie to the session with cookie_name and cookie_value with expiry_time and domain_name and path ",
    description = "Add cookies to currently running session when sent as a key and value pair with expiry_time,domain name and path.Expiry time should be in this sample format -Thu, 18 Dec 2023 11:00:00 UTC",
    applicationType = ApplicationType.MOBILE_WEB)

public class AddCookieWithDomainAndPath extends com.testsigma.addons.cookie_actions.web.AddCookieWithDomainAndPath {
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
    super.setCookieName(cookieName);
    super.setCookieValue(cookieValue);
    super.setCookieExpiry(cookieExpiry);
    super.setCookieDomain(cookieDomain);
    super.setCookiePath(cookiePath);
    return super.execute();
  }
}