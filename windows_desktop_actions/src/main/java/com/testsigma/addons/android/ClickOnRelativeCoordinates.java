package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.event.InputEvent;

@Data
@Action(actionText = "Click on Relative Coordinates, x-coordinate: X-Value, y-coordinate: Y-Value (x and y values are relative to screen dimension in percentages, Ex: 50,50 is the center of the screen)",
        description = "Click on the screen at the given relative coordinates. The x and y values are relative to the screen dimension in percentages. For example, 50,50 is the center of the screen.",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class ClickOnRelativeCoordinates extends AndroidAction {
    @TestData(reference = "X-Value")
    private com.testsigma.sdk.TestData xCoordinate;
    @TestData(reference = "Y-Value")
    private com.testsigma.sdk.TestData yCoordinate;
    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        try{
            Robot robot = new Robot();

            // Get the screen size
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double width = screenSize.getWidth();
            double height = screenSize.getHeight();

            // Calculate the coordinates based on the input percentages
            // For the center, xPercent and yPercent would be 50, 50
            int xPercent = Integer.parseInt(this.xCoordinate.getValue().toString());
            int yPercent = Integer.parseInt(this.yCoordinate.getValue().toString());
            int x = (int) (width * xPercent / 100);
            int y = (int) (height * yPercent / 100);

            // Move the mouse cursor to the calculated coordinates
            robot.mouseMove(x, y);

            // Simulate a mouse click (press and release)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);


        } catch (Exception error) {
            result = Result.FAILED;
            logger.info("Unable to do a mouse press on given location "+ ExceptionUtils.getStackTrace(error));
            setErrorMessage("Unable to do a mouse press on given location:"+error.getMessage());
        }
        return result;

    }
}
