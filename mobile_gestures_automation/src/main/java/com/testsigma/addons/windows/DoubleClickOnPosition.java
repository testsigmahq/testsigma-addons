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
        actionText = "Double click on the given coordinates on the screen. X: X-Coordinate, Y: Y-Coordinate, Touch hold time in Milliseconds: Touch-Hold-Time, Time gap between touch in Milliseconds: Touch-Gap",
        description = "Performs a double click at the specified absolute coordinates on a Windows desktop screen",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = false
)
public class DoubleClickOnPosition extends WindowsAction {

    @TestData(reference = "X-Coordinate")
    private com.testsigma.sdk.TestData xCoordinate;

    @TestData(reference = "Y-Coordinate")
    private com.testsigma.sdk.TestData yCoordinate;

    @TestData(reference = "Touch-Hold-Time")
    private com.testsigma.sdk.TestData touchHoldTime;

    @TestData(reference = "Touch-Gap")
    private com.testsigma.sdk.TestData touchGap;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            Robot robot = new Robot();

            // Parse coordinates and times from TestData
            int xPosition = Integer.parseInt(xCoordinate.getValue().toString());
            int yPosition = Integer.parseInt(yCoordinate.getValue().toString());
            int touchHoldTimeInt = Integer.parseInt(touchHoldTime.getValue().toString());
            int touchGapInt = Integer.parseInt(touchGap.getValue().toString());

            logger.info("Initiating double-click at coordinates X: " + xPosition + ", Y: " + yPosition);

            // Move mouse to the specified coordinates
            robot.mouseMove(xPosition, yPosition);

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

            setSuccessMessage("Successfully double-clicked on the given coordinates on the screen. X: " + xPosition + ", Y: " + yPosition);
        } catch (Exception e) {
            logger.info("Error: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while executing the action: " + e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }
}