package com.testsigma.addons.ios;

import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

@Data
@Action(actionText = "Swipe the element element-locator into view max swipes testdata",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class SwipeElementIntoView extends IOSAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData;

  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    // Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    try {
      IOSDriver iosDriver = (IOSDriver) this.driver;
      int maxSwipes = Integer.parseInt(testData.getValue().toString());
      boolean elementFound = false;

      for (int i = 0; i < maxSwipes; i++) {
        try {
          WebElement foundElement = element.getElement();
          if (foundElement.isDisplayed()) {
            elementFound = true;
            break;
          }
        } catch (Exception e) {
          // Element not found, continue swiping
        }

        logger.info("Element not found. Swiping... (" + (i + 1) + "/" + maxSwipes + ")");
        swipeUp(iosDriver);
      }

      if (!elementFound) {
        // Check one last time after the final swipe
        try {
          if (element.getElement().isDisplayed()) {
            elementFound = true;
          }
        } catch (Exception e) {
          // Ignore
        }
      }

      if (!elementFound) {
        setErrorMessage("Element not found after " + maxSwipes + " swipes.");
        result = com.testsigma.sdk.Result.FAILED;
      } else {
        logger.info("Element found.");
      }

    } catch (Exception e) {
      logger.warn("Exception occurred while executing Swipe element: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception occurred while executing Swipe element: " + ExceptionUtils.getMessage(e));
      result = com.testsigma.sdk.Result.FAILED;

    }
    return result;
  }

  private void swipeUp(IOSDriver driver) {
    Dimension size = driver.manage().window().getSize();
    int startX = (int) (size.width * 0.50);
    int startY = (int) (size.height * 0.80);
    int endY = (int) (size.height * 0.20);

    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence swipe = new Sequence(finger, 1);

    swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
    swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    swipe.addAction(finger.createPointerMove(Duration.ofSeconds(5), PointerInput.Origin.viewport(), startX, endY));
    swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Collections.singletonList(swipe));
  }
}