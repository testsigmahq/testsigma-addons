package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Store the text from element-locator in values-count variable",
        description = "Stores run time data",
        applicationType = ApplicationType.WEB)
public class MySecondWebAction extends WebAction {

  @TestData(reference = "values-count")
  private com.testsigma.sdk.TestData testData;
  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    // Try use of run time data
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    logger.debug("element locator with : "+ this.element.getValue() +" by:"+ this.element.getBy() + ", test-data: "+ this.testData.getValue());
    WebElement webElement = element.getElement();
    runTimeData = new com.testsigma.sdk.RunTimeData();
    runTimeData.setValue(webElement.getText());
    runTimeData.setKey(testData.getValue().toString());
    setSuccessMessage("Successfully stored "+webElement.getText()+" into ::"+testData.getValue());
    return result;
  }
}