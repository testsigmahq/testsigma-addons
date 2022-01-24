package com.testsigma.addons.mobile_gestures.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.offset.ElementOption;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Tap and Hold element1 and Drop it on element2", applicationType = ApplicationType.ANDROID)
public class TapAndHoldAndDropOnDestination extends AndroidAction {


    @Element(reference = "element1")
    private com.testsigma.sdk.Element element1;
    @Element(reference = "element2")
    private com.testsigma.sdk.Element element2;


    @Override
    protected Result execute() throws NoSuchElementException {
        //Your Awesome code starts here

        /* @Author - Amit
        What's Inside?
        Drag and Drop from one postion to other where we do click and hold operation for certain duration
         */
        Result result = Result.SUCCESS;

        logger.info("Initiating execution");
        logger.debug("First Element: " + this.element1.getValue() + " by:" + this.element1.getBy() + "Second Element: " + this.element2.getValue() + "by:" + this.element2.getBy());
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        WebElement webElement1 = element1.getElement();
        WebElement webElement2 = element2.getElement();
        if (webElement1.isDisplayed() && webElement2.isDisplayed()) {
            logger.info("Element is displayed, entering data");
            setSuccessMessage(String.format("Element Found Proceeding next actions"));
        } else {
            result = Result.FAILED;
            logger.warn("Element with locator %s is not visible ::" + element1.getBy() + "::" + element1.getValue() + element2.getValue() + element2.getBy());
            setErrorMessage(String.format("Element with locator %s is not visible", element1.getBy() + "" + element2.getBy()));
        }
        logger.info("Starting Operation");
        TouchAction action = new TouchAction(androidDriver);
        action.longPress(new LongPressOptions().withElement(new
                ElementOption().withElement(webElement1))).perform();
        action.moveTo(new ElementOption().withElement(webElement2)).release().perform();
        setSuccessMessage("Successfully Dragged and Dropped");

        return result;
    }
}

