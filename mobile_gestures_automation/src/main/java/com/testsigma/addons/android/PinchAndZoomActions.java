package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Perform Pinch or Zoom or two finger scroll action on the screen. First finger start point: Start-Point-1, Second finger start point: Start-Point-2, First finger end point: End-Point-1, Second finger end point: End-Point-2 .All points are in x,y format where x is the Percentage of width and y is the percentage of height. Ex. Point: 50,50 (center of the screen)",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class PinchAndZoomActions extends AndroidAction {

/*  @TestData(reference = "Zoom-Action",allowedValues = {"Pinch","Zoom"})
  private com.testsigma.sdk.TestData zoomAction;*/

    @TestData(reference = "Start-Point-1")
    private com.testsigma.sdk.TestData startPoint1;

    @TestData(reference = "Start-Point-2")
    private com.testsigma.sdk.TestData startPoint2;

    @TestData(reference = "End-Point-1")
    private com.testsigma.sdk.TestData endPoint1;

    @TestData(reference = "End-Point-2")
    private com.testsigma.sdk.TestData endPoint2;

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
            int[] start_points_1 = computePoints(startPoint1.getValue().toString(), screenSize);
            int[] start_points_2 = computePoints(startPoint2.getValue().toString(), screenSize);
            int[] end_points_1 = computePoints(endPoint1.getValue().toString(), screenSize);
            int[] end_points_2 = computePoints(endPoint2.getValue().toString(), screenSize);
            logger.info("Start Points 1: " + start_points_1[0] + "," + start_points_1[1]);
            logger.info("Start Points 2: " + start_points_2[0] + "," + start_points_2[1]);
            logger.info("End Points 1: " + end_points_1[0] + "," + end_points_1[1]);
            logger.info("End Points 2: " + end_points_2[0] + "," + end_points_2[1]);
            builder.append(" Computed Start Point Finger-1: ").append(start_points_1[0]).append(",").append(start_points_1[1]);
            builder.append(" Computed Start Point Finger-2: ").append(start_points_2[0]).append(",").append(start_points_2[1]);
            builder.append(" Computed End Point 1: ").append(end_points_1[0]).append(",").append(end_points_1[1]);
            builder.append(" Computed End Point 2: ").append(end_points_2[0]).append(",").append(end_points_2[1]);

            PointerInput pointer_1 = new PointerInput(TOUCH, "finger1");
            PointerInput pointer_2 = new PointerInput(TOUCH, "finger2");
            Sequence swipe_1 = new Sequence(pointer_1, 1)
                    .addAction(pointer_1.createPointerMove(ofMillis(0), viewport(), start_points_1[0], start_points_1[1]))
                    .addAction(pointer_1.createPointerDown(LEFT.asArg()))
                    .addAction(pointer_1.createPointerMove(Duration.ofMillis(500), viewport(), end_points_1[0], end_points_1[1]))
                    .addAction(pointer_1.createPointerUp(LEFT.asArg()));
            Sequence swipe_2 = new Sequence(pointer_2, 1)
                    .addAction(pointer_2.createPointerMove(ofMillis(0), viewport(), start_points_2[0], start_points_2[1]))
                    .addAction(pointer_2.createPointerDown(LEFT.asArg()))
                    .addAction(pointer_2.createPointerMove(Duration.ofMillis(500), viewport(), end_points_2[0], end_points_2[1]))
                    .addAction(pointer_2.createPointerUp(LEFT.asArg()));
            androidDriver.perform(Arrays.asList(swipe_1, swipe_2));
            setSuccessMessage("Successfully Performed Pinch or Zoom action on the screen.<>br" + builder.toString());


        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Error occurred:" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform Zoom on the screen:" + e.getMessage() + "<br>" + builder.toString());

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