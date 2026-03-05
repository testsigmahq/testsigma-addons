package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Clear the text from element-locator using backspace",
        description = "Clears the text from an input field using backspaces",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ClearUsingBackspace extends WebAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {

    com.testsigma.sdk.Result result;

    try {

      logger.info("Initiating execution");
      logger.info("element locator with : " + this.element.getValue() + " by:" + this.element.getBy());

      WebElement webElement = element.getElement();

      webElement.click();
      webElement.sendKeys(Keys.END);

      String text = webElement.getAttribute("value");

      logger.info("text : " + text);

      if (text != null) {
        for (int i = 0; i < text.length(); i++) {
          webElement.sendKeys(Keys.BACK_SPACE);
        }
      }

      logger.info("Successfully cleared the input field using backspace");
      setSuccessMessage("Successfully cleared the input field using backspace");
      result = com.testsigma.sdk.Result.SUCCESS;

    } catch (Exception e) {
      logger.warn("Failed to clear the input field: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to clear the input field due to: " + ExceptionUtils.getMessage(e));
      result = com.testsigma.sdk.Result.FAILED;

    }

    return result;
  }
}