package com.testsigma.addons.debug.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

@Data
@Action(actionText = "Print total number of available options from the element_locator list",
    description = "Prints total number of element in the list",
    applicationType = ApplicationType.WEB)
public class PrintOptionsCount extends WebAction {


  @Element(reference = "element_locator")
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
      Select se = new Select(webElement);
      List<WebElement> ele = se.getOptions();
      int size = ele.size();
      setSuccessMessage("Number of options present in the list is " + size);
      System.out.println("Text from the element is" + size);
    } else {
      logger.info("Operation Failed Please check if element is correct or page has loaded");
      setErrorMessage("Could get size of list from the element Check Logger for more info. Element may not be displayed or doesnot have a select tag associated with it");
      result = com.testsigma.sdk.Result.FAILED;

    }

    return result;

  }
}

