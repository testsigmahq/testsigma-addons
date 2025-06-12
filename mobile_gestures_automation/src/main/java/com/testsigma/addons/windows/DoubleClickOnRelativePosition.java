package com.testsigma.addons.windows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.InputEvent;

@Data
@Action(
        actionText = "Double click on the given Relative coordinates on the screen. Relative-Coords (%width,%height. Ex: 50,50), Touch hold time in Milliseconds: Touch-Hold-Time, Time gap between touch in Milliseconds: Touch-Gap",
        description = "Performs a double click at the specified relative coordinates on a Windows desktop screen",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = false
)

public class DoubleClickOnRelativePosition extends WindowsAction {

  @TestData(reference = "Relative-Coords")
  private com.testsigma.sdk.TestData coords;

  @TestData(reference = "Touch-Hold-Time")
  private com.testsigma.sdk.TestData touchHoldTime;

  @TestData(reference = "Touch-Gap")
  private com.testsigma.sdk.TestData touchGap;

  private final StringBuilder builder = new StringBuilder();

  @Override
  protected Result execute() {
    Result result = Result.SUCCESS;
    try {
      Robot robot = new Robot();
      // Get screen size
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int screenWidth = (int) screenSize.getWidth();
      int screenHeight = (int) screenSize.getHeight();
      builder.append("Screen Width: ").append(screenWidth);
      builder.append(" Screen Height: ").append(screenHeight);
      logger.info("Screen Width: " + screenWidth);
      logger.info("Screen Height: " + screenHeight);

      // Compute absolute coordinates from relative percentages
      int[] points = computePoints(coords.getValue().toString(), screenSize);

      // Parse touch hold time and touch gap
      int touchHoldTimeInt = Double.valueOf(Double.parseDouble(touchHoldTime.getValue().toString())).intValue();
      int touchGapInt = Double.valueOf(Double.parseDouble(touchGap.getValue().toString())).intValue();

      logger.info("Initiating double-click at coordinates X: " + points[0] + ", Y: " + points[1]);

      // Move mouse to the computed coordinates
      robot.mouseMove(points[0], points[1]);

      // Perform first click
      robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
      robot.delay(touchHoldTimeInt); // Hold the mouse button
      robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

      // Wait for the gap between clicks
      robot.delay(touchGapInt);

      // Perform second click
      robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
      robot.delay(touchHoldTimeInt); // Hold the mouse button
      robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

      setSuccessMessage("Successfully double-clicked on the given coordinates on the screen. X: " + points[0] + ", Y: " + points[1]);
    } catch (Exception e) {
      logger.info("Error: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception occurred while executing the action: " + e.getMessage());
      result = Result.FAILED;
    }
    return result;
  }

  private int[] computePoints(String pointInPercent, Dimension screenSize) throws Exception {
    int[] points = new int[2];
    points[0] = (int) (screenSize.getWidth() * Double.parseDouble(pointInPercent.split(",")[0]) / 100);
    points[1] = (int) (screenSize.getHeight() * Double.parseDouble(pointInPercent.split(",")[1]) / 100);
    if (points[0] <= 0 || points[1] <= 0) {
      throw new Exception("Invalid point provided: " + pointInPercent + ". Points should be in x,y format where x is the percentage of width and y is the percentage of height. Ex. Point: 50,50 (center of the screen)");
    }
    points[0] = Math.min(points[0], (int) screenSize.getWidth() - 10);
    points[1] = Math.min(points[1], (int) screenSize.getHeight() - 10);
    return points;
  }
}