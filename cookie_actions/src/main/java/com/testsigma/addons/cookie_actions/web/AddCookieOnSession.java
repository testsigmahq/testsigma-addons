package com.testsigma.addons.cookie_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

import java.util.Date;

@Data
@Action(actionText = "Add Cookies to the session with Key(Cookie_Name) and Value(Cookie_Value)",
    description = "Add cookies to currently running session when sent as a key and value pair",
    applicationType = ApplicationType.WEB)

public class AddCookieOnSession extends WebAction {


  @TestData(reference = "Key")
  private com.testsigma.sdk.TestData cookieName;

  @TestData(reference = "Value")
  private com.testsigma.sdk.TestData cookieValue;


  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    
    logger.info("Initiating execution and starting to add cookies");

    try {
    	//System.out.println(this.getCookieName().getValue().toString());
//      Cookie name = new Cookie(cookieName.getValue().toString(), cookieValue.getValue().toString());
//      driver.manage().addCookie(name);
//      setSuccessMessage("Cookies with key " + this.cookieName.getValue().toString() + " and value" + this.cookieValue.getValue().toString() + "added Successfully");
      logger.info("Success in adding cookies setting status to pass");
      Date today = new Date();
      long timeInLong = today.getTime() + 10 * 24 * 60 * 60 * 1000;
      Date tenDaysFromNow = new Date(timeInLong);
      JavascriptExecutor js = (JavascriptExecutor) driver;
      //js.executeScript("document.cookie = \"" + cookieName.getValue().toString() + "=" + cookieValue.getValue().toString() + "; expires=" + tenDaysFromNow + "; domain=*; path=/\";");
      js.executeScript("document.cookie = \"" + this.getCookieName().getValue() + "=" + this.getCookieValue().getValue() + "; expires=" + tenDaysFromNow + "; domain=*; path=/\";");

    } catch (Exception e) {
      result = Result.FAILED;
      setErrorMessage("Could not add cookies with given key  " + cookieName.getValue().toString() + "  and value   " + cookieValue.getValue().toString());
      logger.debug(e.getMessage() + e.getCause());
    }
    return result;
  }
}