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
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

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

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO,PointerInput.Origin.viewport(), x_offset, y_offset));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        androidDriver.perform(Collections.singletonList(tap));
        setSuccessMessage("Successfully tapped the second character in the element");
      } catch (Exception e) {
        result = com.testsigma.sdk.Result.FAILED;
        logger.warn("Failed to tap the second character in the element " + ExceptionUtils.getStackTrace(e));
        setErrorMessage("Failed to tap the second character in the element: " + ExceptionUtils.getMessage(e));
      }
    }else{
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Element with locator %s is not visible ::"+element.getBy()+"::"+element.getValue());
      setErrorMessage(String.format("Element with locator %s is not visible",element.getBy()));
    }
    return result;
  }
}