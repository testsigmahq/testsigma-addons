package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Double click on the given coordinates on the screen. X: X-Coordinate, Y: Y-Coordinate, Touch hold time in Milliseconds: Touch-Hold-Time, Time gap between touch in Milliseconds: Touch-Gap",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class DoubleClickOnPosition extends AndroidAction {

  @TestData(reference = "X-Coordinate")
  private com.testsigma.sdk.TestData xCoordinate;

    @TestData(reference = "Y-Coordinate")
    private com.testsigma.sdk.TestData yCoordinate;

    @TestData(reference = "Touch-Hold-Time")
    private com.testsigma.sdk.TestData touchHoldTime;

    @TestData(reference = "Touch-Gap")
    private com.testsigma.sdk.TestData touchGap;


  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    AndroidDriver androidDriver = (AndroidDriver) driver;
    try {
      int xPosition = Double.valueOf(Double.parseDouble(xCoordinate.getValue().toString())).intValue();
      int yPosition = Double.valueOf(Double.parseDouble(yCoordinate.getValue().toString())).intValue();
      int touchHoldTimeInt = Double.valueOf(Double.parseDouble(touchHoldTime.getValue().toString())).intValue();
        int touchGapInt = Double.valueOf(Double.parseDouble(touchGap.getValue().toString())).intValue();
      logger.info("Initiating execution");
      PointerInput pointer = new PointerInput(TOUCH, "finger");
      Sequence tap = new Sequence(pointer, 1)
              .addAction(pointer.createPointerMove(ofMillis(0), viewport(), xPosition, yPosition))
              .addAction(pointer.createPointerDown(LEFT.asArg()))
              .addAction(new Pause(pointer, ofMillis(touchHoldTimeInt)))
              .addAction(pointer.createPointerUp(LEFT.asArg()))
              .addAction(new Pause(pointer, ofMillis(touchGapInt)))
              .addAction(pointer.createPointerDown(LEFT.asArg()))
              .addAction(new Pause(pointer, ofMillis(touchHoldTimeInt)))
              .addAction(pointer.createPointerUp(LEFT.asArg()));

      androidDriver.perform(Arrays.asList(tap));
      setSuccessMessage("Successfully Double clicked on the given coordinates on the screen. X: "+xPosition+", Y: "+yPosition);
    }catch (Exception e){
      logger.info("Error::"+ ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception occurred while executing the action: "+e.getMessage());
      result = com.testsigma.sdk.Result.FAILED;

    }


    return result;
  }
}