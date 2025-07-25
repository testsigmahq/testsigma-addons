package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Data
@Action(
        actionText = "Wait for page load and click on element element-locator, if clickable and displayed on screen, within timeoutInSecs s",
        description = "Waits for the page to be completely loaded and then waits for the element to be both visible and clickable within the timeout, then clicks it",
        applicationType = ApplicationType.WEB
)
public class ClickIfVisibleAndClickable extends WebAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element elementLocator;

  @TestData(reference = "timeoutInSecs")
  private com.testsigma.sdk.TestData timeoutInSecs;

  @Override
  public Result execute() throws NoSuchElementException {
    logger.info("Initiating action execution...");

    Result result = Result.SUCCESS;
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(timeoutInSecs.getValue().toString())));

      // Wait for page to be loaded
      logger.info("Waiting for page to be loaded...");
      wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
      logger.info("Page has been loaded successfully.");

      logger.info("Element locator: " + elementLocator.getElement());
      logger.info("Timeout in secs: " + timeoutInSecs.getValue().toString());

      logger.info("Waiting for the element to be clickable.");
      WebElement element = wait.until(ExpectedConditions.elementToBeClickable(elementLocator.getElement()));

      element.click();

      setSuccessMessage("Successfully waited for the page to load and clicked on the element within " + timeoutInSecs.getValue().toString() + " seconds.");

    } catch (Exception e) {
      logger.warn("Failed to click on the element due to an unexpected error. Error :" + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to wait for page load and click on the element. Error : " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }

    return result;
  }
}