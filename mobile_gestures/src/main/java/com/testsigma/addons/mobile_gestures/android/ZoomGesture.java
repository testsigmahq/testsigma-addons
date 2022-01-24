package com.testsigma.addons.mobile_gestures.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

@Data
@Action(actionText = "Zoom on the element with scale scale(scale>=2 )", applicationType = ApplicationType.ANDROID)
public class ZoomGesture extends AndroidAction {


    @TestData(reference = "scale")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here

        /* @Author - Amit
        What's Inside?
        This does zoom action on certain element by taking its coordinate";

         */
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        logger.info("Initiating execution");
        logger.debug("Element: " + this.element.getValue() + " by:" + this.element.getBy());
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        WebElement webElement = element.getElement();
        if (webElement.isDisplayed()) {
            logger.info("Element is displayed, Starting action");
            setSuccessMessage(String.format("Element Found Proceeding next actions"));
        } else {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Element with locator %s is not visible ::" + element.getBy() + "::" + element.getValue());
            setErrorMessage(String.format("Element with locator %s is not visible", element.getBy()));
        }

        try {

            final PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            final PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");
            final Dimension size = webElement.getSize();
            final Point upperLeft = webElement.getLocation();
            final Point center = new Point(upperLeft.getX() + size.getWidth() / 2, upperLeft.getY() + size.getHeight() / 2);
            final int yOffset = center.getY() - upperLeft.getY();
            final Sequence pinchAndZoom1 = new Sequence(finger, 0);
            pinchAndZoom1.addAction(finger.createPointerMove(Duration.ofMillis(0L), PointerInput.Origin.viewport(), center.getX(), center.getY()));
            pinchAndZoom1.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            pinchAndZoom1.addAction(new Pause(finger, Duration.ofMillis(100L)));
            pinchAndZoom1.addAction(finger.createPointerMove(Duration.ofMillis(100L), PointerInput.Origin.viewport(), center.getX(), center.getY() - yOffset));
            pinchAndZoom1.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            final Sequence pinchAndZoom2 = new Sequence(finger2, 0);
            pinchAndZoom2.addAction(finger2.createPointerMove(Duration.ofMillis(0L), PointerInput.Origin.viewport(), center.getX(), center.getY()));
            pinchAndZoom2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            pinchAndZoom2.addAction(new Pause(finger, Duration.ofMillis(100L)));
            pinchAndZoom2.addAction(finger2.createPointerMove(Duration.ofMillis(100L), PointerInput.Origin.viewport(), center.getX(), center.getY() + yOffset));
            pinchAndZoom2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            for (int scaleIndex = 0; scaleIndex < Integer.parseInt(testData.getValue().toString()); ++scaleIndex) {
                androidDriver.perform(Arrays.asList(pinchAndZoom1, pinchAndZoom2));
            }
            setSuccessMessage("Zoom Gesture Successfully Performed");
        } catch (Exception e) {
            logger.info(e.getMessage());
            setErrorMessage("Zoom Failed" + e.getCause().toString());
            StringBuffer sb = new StringBuffer();
            e.printStackTrace();
        }

        return result;

    }
}


