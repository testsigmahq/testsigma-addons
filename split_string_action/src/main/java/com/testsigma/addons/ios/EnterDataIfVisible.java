package com.testsigma.addons.ios;


import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Enter test-data in element element-locator if visible", applicationType = ApplicationType.IOS)
public class EnterDataIfVisible extends IOSAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData testData;
  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    logger.debug("Element Locator: "+ this.element.getValue() +" by:"+ this.element.getBy() + ", test-data: "+ this.testData.getValue());
    IOSDriver iosDriver = (IOSDriver)this.driver;
    WebElement webElement = element.getElement();//Alternate way iosDriver.findElement(element.getBy())
    if(webElement.isDisplayed()){
      logger.info("Element is displayed, entering data");
      webElement.sendKeys(testData.getValue().toString());
      setSuccessMessage(String.format("Successfully entered %s on element with %s::%s", testData.getValue().toString(), element.getBy(), element.getValue()));
    }else{
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Element with locator %s is not visible ::"+element.getBy()+"::"+element.getValue());
      setErrorMessage(String.format("Element with locator %s is not visible",element.getBy()));
    }
    return result;
  }
}