package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.NoSuchElementException;

@Action(actionText = "Click on fixed coordinates x-coordinate y-coordinate",
        description = "This action clicks on the screen at the specified fixed pixel coordinates. " +
                "The coordinates should be provided as comma-separated values (e.g., '100,200'). " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Click on fixed coordinates (pixel values)",
        useCustomScreenshot = true)
public class ClickOnFixedCoordinates extends WindowsAdvancedAction {

    @TestData(reference = "x-coordinate")
    private com.testsigma.sdk.TestData xCoordinate;

    @TestData(reference = "y-coordinate")
    private com.testsigma.sdk.TestData yCoordinate;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Click On Fixed Coordinates: Starting Execution ===");
        
        try {
            String xCoordinateValue = xCoordinate.getValue().toString();
            String yCoordinateValue = yCoordinate.getValue().toString();
            
            logger.info("Clicking on fixed coordinates: " + xCoordinateValue + ", " + yCoordinateValue);
            
            // Validate and parse coordinates
            if (xCoordinateValue == null || xCoordinateValue.trim().isEmpty() || yCoordinateValue == null || yCoordinateValue.trim().isEmpty()) {
                setErrorMessage("Coordinate values cannot be empty");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "click_fixed_coordinates_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            int x, y;
            try {
                x = Integer.parseInt(xCoordinateValue.trim());
                y = Integer.parseInt(yCoordinateValue.trim());
            } catch (NumberFormatException e) {
                setErrorMessage("Invalid coordinate values. Coordinates must be valid integers.");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "click_fixed_coordinates_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Validate coordinates are within screen bounds
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (x < 0 || x >= screenSize.width || y < 0 || y >= screenSize.height) {
                setErrorMessage(String.format(
                    "Coordinates (%d, %d) are outside screen bounds. Screen size: %dx%d",
                    x, y, screenSize.width, screenSize.height
                ));
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "click_fixed_coordinates_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            logger.info("Clicking at coordinates: (" + x + ", " + y + ")");
            
            // Perform the click
            performClickWithRobot(x, y);
            
            String successMessage = String.format(
                "Successfully clicked at fixed coordinates: x-<b>%d</b>, y-<b>%d</b>",
                x, y
            );
            setSuccessMessage(successMessage);
            logger.info("Successfully clicked at coordinates: (" + x + ", " + y + ")");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "click_fixed_coordinates_screenshot", logger);
            
            return Result.SUCCESS;
            
        } catch (Exception e) {
            String errorMessage = "Error clicking on fixed coordinates: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("Exception during click operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "click_fixed_coordinates_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    /**
     * Performs click using Robot with appropriate delays
     */
    private void performClickWithRobot(int x, int y) throws Exception {
        Robot robot = new Robot();

        // Move mouse to the target location
        logger.info("Moving mouse to coordinates (" + x + ", " + y + ")");
        robot.mouseMove(x, y);
        Thread.sleep(200); // Delay to ensure mouse is positioned

        // Press mouse button
        logger.info("Pressing mouse button");
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(100); // Delay between press and release

        // Release mouse button
        logger.info("Releasing mouse button");
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(200); // Delay after click completion

        logger.info("Click completed successfully");
    }
}
