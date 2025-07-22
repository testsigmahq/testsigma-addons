package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Data
@Action(
        actionText = "Click on element element-locator, if clickable and displayed on screen, within wait time of timeoutInSecs s",
        description = "Waits for the element to be both visible and clickable within the timeout, then clicks it",
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
      logger.info("Element locator: " + elementLocator.getElement());
      logger.info("Timeout in secs: " + timeoutInSecs.getValue().toString());
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(timeoutInSecs.getValue().toString())));
      WebElement element = wait.until(ExpectedConditions.elementToBeClickable(elementLocator.getElement()));

      element.click();

      setSuccessMessage("Successfully clicked on the element within " + timeoutInSecs.getValue().toString() + " seconds.");

    } catch (Exception e) {
      logger.warn("Failed to click on the element due to an unexpected error. Error :" + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to click on the element due to an unexpected error. Error : " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }

    return result;
  }
}
