package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Tap the second character in the element element-locator",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class TapSecondCharacter extends AndroidAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    logger.debug("Element Locator: "+ this.element.getValue() +" by:"+ this.element.getBy());
    AndroidDriver androidDriver = (AndroidDriver)this.driver;
    WebElement webElement = element.getElement();
    if(webElement.isDisplayed()){
      logger.info("Element is displayed, tapping the second character");
      try {
        int elementLeft = webElement.getLocation().getX();
        int elementTop = webElement.getLocation().getY();
        int elementWidth = webElement.getSize().getWidth();
        int elementHeight = webElement.getSize().getHeight();

        int y_offset = elementTop + (int)(elementHeight * 0.2);
        int x_offset = elementLeft + (int)(elementWidth * 0.1);

        TouchAction touchAction = new TouchAction(androidDriver);
        touchAction.tap(PointOption.point(x_offset, y_offset)).perform();
        setSuccessMessage("Successfully tapped the second character in the element");
      } catch (Exception e) {
        result = com.testsigma.sdk.Result.FAILED;
        logger.warn("Failed to tap the second character in the element " + ExceptionUtils.getStackTrace(e));
        setErrorMessage("Failed to tap the second character in the element: " + ExceptionUtils.getStackTrace(e));
      }
    }else{
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Element with locator %s is not visible ::"+element.getBy()+"::"+element.getValue());
      setErrorMessage(String.format("Element with locator %s is not visible",element.getBy()));
    }
    return result;
  }
}