package com.testsigma.addons.mobile_gestures.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.Duration;

@Data
@Action(actionText = "Zoom on the element with using Multitouch actions", applicationType = ApplicationType.ANDROID)
public class ZoomGestureMultitouch extends AndroidAction {


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
        logger.debug("ui-identifier: " + this.element.getValue() + " by:" + this.element.getBy());
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

        Dimension dim = webElement.getSize();
        int width = dim.width;
        int height = dim.height;
        int firstTouchXcoordinate_Start = (int) (width * .5);
        int firstTouchYcoordinate_Start = (int) (height * .4);

        int firstTouchXcoordinate_End = (int) (width * .1);
        int firstTouchYcoordinate_End = (int) (height * .1);

        int secondTouchXcoordinate_Start = (int) (width * .5);
        int secondTouchYcoordinate_Start = (int) (height * .6);

        int secondTouchXcoordinate_End = (int) (width * .9);
        int secondTouchYcoordinate_End = (int) (height * .9);

        TouchAction touch1 = new TouchAction(androidDriver);
        TouchAction touch2 = new TouchAction(androidDriver);

        touch1.longPress(PointOption.point(firstTouchXcoordinate_Start, firstTouchYcoordinate_Start))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(2000)))
                .moveTo(PointOption.point(firstTouchXcoordinate_End, firstTouchYcoordinate_End));

        touch2.longPress(PointOption.point(secondTouchXcoordinate_Start, secondTouchYcoordinate_Start))
                .waitAction(WaitOptions.waitOptions(Duration.ofMillis(2000)))
                .moveTo(PointOption.point(secondTouchXcoordinate_End, secondTouchYcoordinate_End));

        MultiTouchAction multi = new MultiTouchAction(androidDriver);
        multi.add(touch1).add(touch2).perform();

        return result;

    }
}



