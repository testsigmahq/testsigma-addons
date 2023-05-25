package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Action(actionText = "Enter test-data in element element-locator if visible", applicationType = ApplicationType.ANDROID)
public class MyFirstAndroidAction extends AndroidAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData testData;
  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    logger.debug("Element locator: "+ this.element.getValue() +" by:"+ this.element.getBy() + ", test-data: "+ this.testData.getValue());
    AndroidDriver androidDriver = (AndroidDriver)this.driver;
    WebElement webElement = element.getElement();
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