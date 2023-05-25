package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.time.Duration;

@Data
@Action(actionText = "Swipe on screen from source-coordinates to target-coordinates", applicationType = ApplicationType.ANDROID)
public class SwipeOnScreen extends AndroidAction {

    @TestData(reference = "source-coordinates")
    private com.testsigma.sdk.TestData sourceCoordinates;
    @TestData(reference = "target-coordinates")
    private com.testsigma.sdk.TestData targetCoordinates;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("source-coordinates: "+ this.sourceCoordinates.getValue() + ", target-coordinates: "+ this.targetCoordinates.getValue());
        AndroidDriver androidDriver = (AndroidDriver)this.driver;
        String[] srcCoordinatesString = sourceCoordinates.getValue().toString().split(",");
        int sourceX = Integer.parseInt(srcCoordinatesString[0]);
        int sourceY = Integer.parseInt(srcCoordinatesString[1]);
        String[] targetCoordinatesString = targetCoordinates.getValue().toString().split(",");
        int targetX = Integer.parseInt(targetCoordinatesString[0]);
        int targetY = Integer.parseInt(targetCoordinatesString[1]);
        logger.info(String.format("Swiping from %s to %s",sourceCoordinates.getValue(),targetCoordinates.getValue()));
        TouchAction swipeTo = new TouchAction(androidDriver);
        Duration d = Duration.ofSeconds(5);
        swipeTo.press(PointOption.point(sourceX, sourceY)).waitAction(WaitOptions.waitOptions(d)).moveTo(PointOption.point(targetX, targetY)).release().perform();
        setSuccessMessage(String.format("Successfully swiped from %s to %s", sourceCoordinates.getValue(), targetCoordinates.getValue()));
        return result;
    }
}