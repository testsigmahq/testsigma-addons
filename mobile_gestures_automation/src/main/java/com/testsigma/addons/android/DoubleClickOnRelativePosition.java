package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Double click on the given Relative coordinates on the screen. Relative-Coords (%width,%height. Ex: 50,50 ), Touch hold time in Milliseconds: Touch-Hold-Time, Time gap between touch in Milliseconds: Touch-Gap",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class DoubleClickOnRelativePosition extends AndroidAction {

    @TestData(reference = "Relative-Coords")
    private com.testsigma.sdk.TestData coords;

    @TestData(reference = "Touch-Hold-Time")
    private com.testsigma.sdk.TestData touchHoldTime;

    @TestData(reference = "Touch-Gap")
    private com.testsigma.sdk.TestData touchGap;

    StringBuilder builder = new StringBuilder();

  @Override
  protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    AndroidDriver androidDriver = (AndroidDriver) driver;
    Dimension screenSize = androidDriver.manage().window().getSize();
    int screenWidth = screenSize.getWidth();
    int screenHeight = screenSize.getHeight();
    builder.append("Screen Width: ").append(screenWidth);
    builder.append(" Screen Height: ").append(screenHeight);
    logger.info("Screen Width: " + screenWidth);
    logger.info("Screen Height: " + screenHeight);
    try {
      int[] start_points_1 = computePoints(coords.getValue().toString(), screenSize);

      int touchHoldTimeInt = Double.valueOf(Double.parseDouble(touchHoldTime.getValue().toString())).intValue();
        int touchGapInt = Double.valueOf(Double.parseDouble(touchGap.getValue().toString())).intValue();
      logger.info("Initiating execution");
      PointerInput pointer = new PointerInput(TOUCH, "finger");
      Sequence tap = new Sequence(pointer, 1)
              .addAction(pointer.createPointerMove(ofMillis(0), viewport(), start_points_1[0], start_points_1[1]))
              .addAction(pointer.createPointerDown(LEFT.asArg()))
              .addAction(new Pause(pointer, ofMillis(touchHoldTimeInt)))
              .addAction(pointer.createPointerUp(LEFT.asArg()))
              .addAction(new Pause(pointer, ofMillis(touchGapInt)))
              .addAction(pointer.createPointerDown(LEFT.asArg()))
              .addAction(new Pause(pointer, ofMillis(touchHoldTimeInt)))
              .addAction(pointer.createPointerUp(LEFT.asArg()));

      androidDriver.perform(Arrays.asList(tap));
      setSuccessMessage("Successfully Double clicked on the given coordinates on the screen. X: "+start_points_1[0]+", Y: "+start_points_1[1]);
    }catch (Exception e){
      logger.info("Error::"+ ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception occurred while executing the action: "+e.getMessage());
      result = com.testsigma.sdk.Result.FAILED;

    }


    return result;
  }
  private int[] computePoints(String pointInPercent, Dimension screenSize) throws Exception {
    int[] points = new int[2];
    points[0] = (int) (screenSize.getWidth() * Double.parseDouble(pointInPercent.split(",")[0]) / 100);
    points[1] = (int) (screenSize.getHeight() * Double.parseDouble(pointInPercent.split(",")[1]) / 100);
    if (points[0] <= 0 || points[1] <= 0) {
      throw new Exception("Invalid point provided" + pointInPercent + ". Points should be in x,y format where x is the Percentage of width and y is the percentage of height. Ex. Point: 50,50 (center of the screen)");
    }
    points[0] = points[0] >= screenSize.getWidth() ? (screenSize.getWidth() - 10) : points[0];
    points[1] = points[1] >= screenSize.getHeight() ? (screenSize.getHeight() - 10) : points[1];
    return points;
  }
}