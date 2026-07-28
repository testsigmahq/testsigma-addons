package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.awt.event.InputEvent;

import java.awt.*;
import java.util.NoSuchElementException;

@Action(actionText = "Click on relative coordinates x-percentage y-percentage",
        description = "This action clicks on the screen at coordinates specified as percentages of the screen dimensions. " +
                "The coordinates should be provided as comma-separated percentage values (e.g., '50,25' for center-left). " +
                "Values should be between 0 and 100. This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Click on relative coordinates (where x & y are percentage of screen width & height)",
        useCustomScreenshot = true)
public class ClickOnRelativeCoordinates extends WindowsAdvancedAction {

    @TestData(reference = "x-percentage")
    private com.testsigma.sdk.TestData xPercentage;

    @TestData(reference = "y-percentage")
    private com.testsigma.sdk.TestData yPercentage;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Click On Relative Coordinates: Starting Execution ===");
        
        try {
            // Validate and parse coordinates
            String xPercentageValue = xPercentage.getValue().toString();
            String yPercentageValue = yPercentage.getValue().toString();
            
            double xPercent, yPercent;
            try {
                xPercent = Double.parseDouble(xPercentageValue.trim());
                yPercent = Double.parseDouble(yPercentageValue.trim());
            } catch (NumberFormatException e) {
                setErrorMessage("Invalid coordinate values. Coordinates must be valid numbers.");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "click_relative_coordinates_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Validate percentage values are within valid range
            if (xPercent < 0 || xPercent > 100 || yPercent < 0 || yPercent > 100) {
                setErrorMessage("Percentage values must be between 0 and 100. Provided values: x=" + xPercent + "%, y=" + yPercent + "%");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "click_relative_coordinates_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Get screen dimensions
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            int screenHeight = screenSize.height;
            
            // Calculate actual pixel coordinates
            int x = (int) Math.round((xPercent / 100.0) * screenWidth);
            int y = (int) Math.round((yPercent / 100.0) * screenHeight);
            
            logger.info("Screen dimensions: " + screenWidth + "x" + screenHeight);
            logger.info("Percentage coordinates: (" + xPercent + "%, " + yPercent + "%)");
            logger.info("Calculated pixel coordinates: (" + x + ", " + y + ")");
            
            // Perform the click
            performClickWithRobot(x, y);
            
            String successMessage = String.format(
                "Successfully clicked at relative coordinates: <b>%.1f%%</b> width, <b>%.1f%%</b> height " +
                "(pixel coordinates: x-<b>%d</b>, y-<b>%d</b>)",
                xPercent, yPercent, x, y
            );
            setSuccessMessage(successMessage);
            logger.info("Successfully clicked at relative coordinates: (" + xPercent + "%, " + yPercent + "%)");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "click_relative_coordinates_screenshot", logger);
            
            return Result.SUCCESS;
            
        } catch (Exception e) {
            String errorMessage = "Error clicking on relative coordinates: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("Exception during click operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "click_relative_coordinates_failure_screenshot", logger);
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
