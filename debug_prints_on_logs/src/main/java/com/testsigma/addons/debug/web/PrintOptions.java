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
@Action(actionText = "Print total number of available options from the element list",
    description = "Prints total number of element in the list",
    applicationType = ApplicationType.WEB)
public class PrintOptions extends WebAction {


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
      Select se = new Select(webElement);
      List<WebElement> ele = se.getOptions();
      int size = ele.size();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i <= size - 1; i++) {
        sb.append(ele.get(i).getText() + " ");
      }
      setSuccessMessage("All the options in the list are " + sb + "  and the count of options is" + size);
      System.out.println("All the options in the list are " + sb);
    } else {
      logger.info("Operation Failed Please check if element is correct or page has loaded");
      setErrorMessage("Could get size of list from the element Check Logger for more info");
      result = com.testsigma.sdk.Result.FAILED;

    }

    return result;

  }
}

