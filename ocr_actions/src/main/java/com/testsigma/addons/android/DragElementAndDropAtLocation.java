package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Move the Element Element-Locator to the position Move-X-Position , Move-Y-Position on the" +
        " screen and perform click operation",
        description = "Moves the element to the given location and performs click operation.",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class DragElementAndDropAtLocation extends AndroidAction {

    @TestData(reference = "Move-X-Position")
    private com.testsigma.sdk.TestData moveXPosition;
    @TestData(reference = "Move-Y-Position")
    private com.testsigma.sdk.TestData moveYPosition;
    @Element(reference = "Element-Locator")
    private com.testsigma.sdk.Element element;


    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        try {

            WebElement srcElement = element.getElement();
            int middleXCoordinate_dragElement = srcElement.getLocation().x + (srcElement.getSize().width / 2);
            int middleYCoordinate_dragElement = srcElement.getLocation().y + (srcElement.getSize().height / 2);
            int middleXCoordinate_dropElement = Integer.parseInt(moveXPosition.getValue().toString());
            int middleYCoordinate_dropElement = Integer.parseInt(moveYPosition.getValue().toString());


            PointerInput Finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(Finger, 1)
                    .addAction(Finger.createPointerMove(ofMillis(0), viewport(),
                            middleXCoordinate_dragElement, middleYCoordinate_dragElement))
                    .addAction(Finger.createPointerDown(LEFT.asArg()))
                    .addAction(Finger.createPointerMove(ofSeconds(5), viewport(),
                            middleXCoordinate_dropElement, middleYCoordinate_dropElement))
                    .addAction(Finger.createPointerUp(LEFT.asArg()));
            androidDriver.perform(Arrays.asList(swipe));

            logger.info("Moved to target element");

            // Create a PointerInput instance for the mouse
            PointerInput mouse = new PointerInput(PointerInput.Kind.MOUSE, "mouse");

            Sequence doubleClickAtCurrentPosition = new Sequence(mouse, 1)
                    .addAction(mouse.createPointerMove(Duration.ofMillis(34),
                            PointerInput.Origin.viewport(),
                            middleXCoordinate_dropElement, middleYCoordinate_dropElement)) // Move cursor to position
                    .addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))  // First press
                    .addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));    // First release

            androidDriver.perform(Arrays.asList(doubleClickAtCurrentPosition));

            logger.info("clicked the target element");
            setSuccessMessage("Successfully Moved the element to given location and performed click operation");
        } catch (Exception e) {

            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Error:: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to move the element:" + e.getMessage());

        }
        return result;
    }
}