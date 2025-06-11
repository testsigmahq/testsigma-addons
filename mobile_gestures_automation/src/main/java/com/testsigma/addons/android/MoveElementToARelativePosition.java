package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Move the Element Element-Locator to a relative position on the screen. Target Position: Relative-Position .The position input is in x,y format where x is the Percentage of width and y is the percentage of height. Ex. Point: 50,50 (center of the screen)",
        description = "",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class MoveElementToARelativePosition extends AndroidAction {

    @TestData(reference = "Relative-Position")
    private com.testsigma.sdk.TestData relativePositionPercent;

    @Element(reference = "Element-Locator")
    private com.testsigma.sdk.Element element;

    StringBuilder builder = new StringBuilder();


    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        AndroidDriver androidDriver = (AndroidDriver)this.driver;
        try  {

            WebElement srcElement = element.getElement();
            Rectangle elementBounds = srcElement.getRect();
            Dimension screenSize = androidDriver.manage().window().getSize();
            int[] end_points_1 = computePoints(relativePositionPercent.getValue().toString(), screenSize);
            builder.append("Screen Width: ").append(screenSize.getWidth());
            builder.append(" Screen Height: ").append(screenSize.getHeight());
            logger.info("Screen Width: " + screenSize.getWidth());
            logger.info("Screen Height: " + screenSize.getHeight());
            builder.append("Target Point:").append(end_points_1[0]).append(",").append(end_points_1[1]);
            logger.info("Target Point:"+end_points_1[0]+","+end_points_1[1]);


            int middleXCoordinate_dragElement = elementBounds.x + (elementBounds.width / 2 );
            int middleYCoordinate_dragElement = elementBounds.y + (elementBounds.height / 2 );


            PointerInput Finger = new PointerInput(PointerInput.Kind.TOUCH,"finger");
            Sequence swipe = new Sequence(Finger,1)
                    .addAction(Finger.createPointerMove(ofMillis(0), viewport(), middleXCoordinate_dragElement, middleYCoordinate_dragElement))
                    .addAction(Finger.createPointerDown(LEFT.asArg()))
                    .addAction(Finger.createPointerMove(ofSeconds(3), viewport(), end_points_1[0], end_points_1[1]))
                    .addAction(Finger.createPointerUp(LEFT.asArg()));
            androidDriver.perform(Arrays.asList(swipe));
            builder.append("<br>Successfully Moved the element to given Position.");
            setSuccessMessage(builder.toString());
        }
        catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Error::"+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to move the element:"+e.getMessage());
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