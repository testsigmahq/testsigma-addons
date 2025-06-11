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

import java.time.Duration;
import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Perform two finger tap on given points on the screen. Fist finger Tap Point: Tap-Point-1 ,Second finger Tap Point: Tap-Point-2, Hold duration in Milliseconds: Hold-Duration .Tap points are in x,y format where x is the Percentage of width and y is the percentage of height. Ex. Point: 50,50 (center of the screen)",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class TwoFingerTapOnPosition extends AndroidAction {

/*  @TestData(reference = "Zoom-Action",allowedValues = {"Pinch","Zoom"})
  private com.testsigma.sdk.TestData zoomAction;*/

    @TestData(reference = "Tap-Point-1")
    private com.testsigma.sdk.TestData tapPoint1;

    @TestData(reference = "Tap-Point-2")
    private com.testsigma.sdk.TestData tapPoint2;

    @TestData(reference = "Hold-Duration")
    private com.testsigma.sdk.TestData holdDuration;

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
            int hold_duration = Integer.parseInt(holdDuration.getValue().toString());
            hold_duration = hold_duration < 0 ? 100 : hold_duration;
            int[] tap_points_1 = computePoints(tapPoint1.getValue().toString(), screenSize);
            int[] tap_points_2 = computePoints(tapPoint2.getValue().toString(), screenSize);

            logger.info("Tap Point: " + tap_points_1[0] + "," + tap_points_1[1]);
            PointerInput pointer_1 = new PointerInput(TOUCH, "finger1");
            Sequence swipe_1 = new Sequence(pointer_1, 1)
                    .addAction(pointer_1.createPointerMove(ofMillis(0), viewport(), tap_points_1[0], tap_points_1[1]))
                    .addAction(new Pause(pointer_1, Duration.ofMillis(300)))
                    .addAction(pointer_1.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(pointer_1, Duration.ofMillis(hold_duration)))
                    //.addAction(pointer_1.createPointerMove(ofSeconds(hold_duration), viewport(), tap_points_1[0], tap_points_1[1]))
                    .addAction(pointer_1.createPointerUp(LEFT.asArg()));

            PointerInput pointer_2 = new PointerInput(TOUCH, "finger2");
            Sequence swipe_2 = new Sequence(pointer_2, 1)
                    .addAction(pointer_2.createPointerMove(ofMillis(0), viewport(), tap_points_2[0], tap_points_2[1]))
                    .addAction(new Pause(pointer_1, Duration.ofMillis(300)))
                    .addAction(pointer_2.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(pointer_2, Duration.ofMillis(hold_duration)))
                    //.addAction(pointer_2.createPointerMove(ofSeconds(hold_duration), viewport(), tap_points_2[0], tap_points_2[1]))
                    .addAction(pointer_2.createPointerUp(LEFT.asArg()));


            androidDriver.perform(Arrays.asList(swipe_1, swipe_2));
            setSuccessMessage("Successfully Performed Long press action on the screen.<>br" + builder.toString());


        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Error occurred:" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform Long press on the screen:" + e.getMessage() + "<br>" + builder.toString());
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
        return points;
    }
}