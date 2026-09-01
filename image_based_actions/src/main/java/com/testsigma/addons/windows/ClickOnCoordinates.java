package com.testsigma.addons.windows;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Action(actionText = "Click on coordinates x-value: x-coordinate, y-value: y-coordinate",
        description = "This action clicks on the screen at the specified coordinates.",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class ClickOnCoordinates extends WindowsAction {

    @TestData(reference = "x-coordinate")
    private com.testsigma.sdk.TestData xCoordinate;

    @TestData(reference = "y-coordinate")
    private com.testsigma.sdk.TestData yCoordinate;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        File screenshotFile = null;
        try {
            String xCoordinateValue = xCoordinate.getValue().toString();
            String yCoordinateValue = yCoordinate.getValue().toString();
            int x = Integer.parseInt(xCoordinateValue);
            int y = Integer.parseInt(yCoordinateValue);

            BufferedImage screenCapture = new Robot().createScreenCapture(
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

            Graphics2D g2d = screenCapture.createGraphics();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - 15, y - 15, 30, 30);
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x - 15, y, x + 15, y);
            g2d.drawLine(x, y - 15, x, y + 15);
            g2d.dispose();

            screenshotFile = File.createTempFile("click_coordinates_screenshot", ".png");
            ImageIO.write(screenCapture, "PNG", screenshotFile);
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);

            logger.info("Clicking on coordinates: " + xCoordinateValue + ", " + yCoordinateValue);
            Robot robot = new Robot();
            robot.mouseMove(x, y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);

            setSuccessMessage("Successfully clicked on coordinates: " + xCoordinateValue + ", " + yCoordinateValue);
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.info("Failed to click on coordinates: " + e.getMessage());
            setErrorMessage("Failed to click on coordinates: " + e.getMessage());
            return Result.FAILED;
        } finally {
            if (screenshotFile != null && screenshotFile.exists()) {
                screenshotFile.delete();
            }
        }
    }
}
