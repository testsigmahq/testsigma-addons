package com.testsigma.addons.debug.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Print text from element",
    description = "Prints text from element",
    applicationType = ApplicationType.MOBILE_WEB)
public class PrintElementTextOnLogs extends com.testsigma.addons.debug.web.PrintElementTextOnLogs {


  @Element(reference = "element")
  private com.testsigma.sdk.Element element;


  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    logger.debug("Element Locator: " + this.element.getValue() + " by:" + this.element.getBy());
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    JavascriptExecutor js = (JavascriptExecutor) driver;
    WebElement webElement = element.getElement();
    if (webElement.isDisplayed() && webElement.isEnabled() && js.executeScript("return document.readyState").equals("complete")) {
      String text = webElement.getText();
      logger.info("Text of the element is" + text);
      setSuccessMessage("Text from the element is" + text);
      System.out.println("Text from the element is" + text);
    } else {
      logger.info("Operation Failed Please check if element is correct or page has loaded");
      setErrorMessage("Could not extract Text from the element Check Logger for more info");
      result = com.testsigma.sdk.Result.FAILED;

    }

    return result;

  }
}
