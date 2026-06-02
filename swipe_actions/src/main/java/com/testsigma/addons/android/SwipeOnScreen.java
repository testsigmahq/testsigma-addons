package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

@Action(actionText = "Swipe in direction for swipe-count number of times",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class SwipeOnScreen extends AndroidAction {

    @TestData(reference = "direction",
              allowedValues = {
                  "LEFT TO RIGHT",
                  "RIGHT TO LEFT",
                  "MIDDLE TO LEFT",
                  "MIDDLE TO RIGHT",
                  "LEFT TO MIDDLE",
                  "RIGHT TO MIDDLE",
                  "TOP TO BOTTOM",
                  "BOTTOM TO TOP",
                  "TOP TO MIDDLE",
                  "MIDDLE TO TOP",
                  "BOTTOM TO MIDDLE"
              })
    private com.testsigma.sdk.TestData direction;

    @TestData(reference = "swipe-count")
    private com.testsigma.sdk.TestData swipeCount;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating SwipeOnScreen execution");

        int count;
        try {
            count = Integer.parseInt(swipeCount.getValue().toString().trim());
        } catch (NumberFormatException e) {
            setErrorMessage("Invalid swipe count: " + swipeCount.getValue() + ". Please provide a valid integer.");
            return com.testsigma.sdk.Result.FAILED;
        }

        if (count <= 0) {
            setErrorMessage("Swipe count must be greater than 0, but got: " + count);
            return com.testsigma.sdk.Result.FAILED;
        }

        String swipeDirection = direction.getValue().toString().trim().toUpperCase();

        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        Dimension size = androidDriver.manage().window().getSize();

        int[] coords = resolveCoordinates(swipeDirection, size);
        if (coords == null) {
            setErrorMessage("Unsupported swipe direction: " + swipeDirection);
            return com.testsigma.sdk.Result.FAILED;
        }

        int startX = coords[0];
        int startY = coords[1];
        int endX   = coords[2];
        int endY   = coords[3];

        logger.debug(String.format("Screen size: %dx%d | Swipe [%s]: (%d,%d) -> (%d,%d) | Repeat: %d",
                size.width, size.height, swipeDirection, startX, startY, endX, endY, count));

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info(String.format("Performing swipe %d of %d [%s]", i + 1, count, swipeDirection));
            Sequence swipe = new Sequence(finger, 1)
                    .addAction(finger.createPointerMove(Duration.ofMillis(0),
                            PointerInput.Origin.viewport(), startX, startY))
                    .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                    .addAction(finger.createPointerMove(Duration.ofSeconds(1),
                            PointerInput.Origin.viewport(), endX, endY))
                    .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            androidDriver.perform(Collections.singletonList(swipe));
        }

        setSuccessMessage(String.format("Successfully swiped [%s] %d time(s)", swipeDirection, count));
        return result;
    }

    /**
     * Maps a direction string to [startX, startY, endX, endY] screen coordinates.
     * Horizontal swipes keep Y at 50% of screen height.
     * Vertical swipes keep X at 50% of screen width.
     * "MIDDLE" refers to the 50% midpoint of the respective axis.
     */
    private int[] resolveCoordinates(String swipeDirection, Dimension size) {
        int w = size.width;
        int h = size.height;

        switch (swipeDirection) {
            case "LEFT TO RIGHT":
                return new int[]{(int) (w * 0.10), (int) (h * 0.50), (int) (w * 0.90), (int) (h * 0.50)};
            case "RIGHT TO LEFT":
                return new int[]{(int) (w * 0.90), (int) (h * 0.50), (int) (w * 0.10), (int) (h * 0.50)};
            case "MIDDLE TO LEFT":
                return new int[]{(int) (w * 0.50), (int) (h * 0.50), (int) (w * 0.10), (int) (h * 0.50)};
            case "MIDDLE TO RIGHT":
                return new int[]{(int) (w * 0.50), (int) (h * 0.50), (int) (w * 0.80), (int) (h * 0.50)};
            case "LEFT TO MIDDLE":
                return new int[]{(int) (w * 0.10), (int) (h * 0.50), (int) (w * 0.50), (int) (h * 0.50)};
            case "RIGHT TO MIDDLE":
                return new int[]{(int) (w * 0.90), (int) (h * 0.50), (int) (w * 0.50), (int) (h * 0.50)};
            case "TOP TO BOTTOM":
                return new int[]{(int) (w * 0.50), (int) (h * 0.10), (int) (w * 0.50), (int) (h * 0.90)};
            case "BOTTOM TO TOP":
                return new int[]{(int) (w * 0.50), (int) (h * 0.90), (int) (w * 0.50), (int) (h * 0.10)};
            case "TOP TO MIDDLE":
                return new int[]{(int) (w * 0.50), (int) (h * 0.10), (int) (w * 0.50), (int) (h * 0.50)};
            case "MIDDLE TO TOP":
                return new int[]{(int) (w * 0.50), (int) (h * 0.50), (int) (w * 0.50), (int) (h * 0.10)};
            case "BOTTOM TO MIDDLE":
                return new int[]{(int) (w * 0.50), (int) (h * 0.90), (int) (w * 0.50), (int) (h * 0.50)};
            default:
                return null;
        }
    }
}
