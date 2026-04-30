package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Element;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Swipe inside the element element-name in direction direction for duration seconds",
        description = "Scrolls inside the given element in the specified direction. The swipe gesture takes the" +
                " given number of seconds to complete. Use longer durations for slower, more deliberate scrolls.",
        applicationType = ApplicationType.ANDROID,
        displayName = "Scroll inside element with direction and duration",
        useCustomScreenshot = false)
public class ScrollInsideElementWithDuration extends AndroidAction {

    @com.testsigma.sdk.annotation.Element(reference = "element-name")
    private Element elementName;

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
                  "BOTTOM TO MIDDLE",
                  "MIDDLE TO BOTTOM"
              })
    private com.testsigma.sdk.TestData direction;

    @TestData(reference = "duration")
    private com.testsigma.sdk.TestData duration;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Starting scroll inside element with direction and duration");

        int durationSeconds;
        try {
            durationSeconds = Integer.parseInt(duration.getValue().toString().trim());
        } catch (NumberFormatException e) {
            setErrorMessage("Invalid duration: '" + duration.getValue() + "'. Please provide a whole number of seconds.");
            return Result.FAILED;
        }

        if (durationSeconds <= 0) {
            setErrorMessage("Duration must be greater than 0, but got: " + durationSeconds);
            return Result.FAILED;
        }

        String swipeDirection = direction.getValue().toString().trim().toUpperCase();

        try {
            org.openqa.selenium.WebElement webElement = elementName.getElement();
            Rectangle rect = webElement.getRect();

            int centerX = rect.x + rect.width / 2;
            int centerY = rect.y + rect.height / 2;
            int left    = rect.x;
            int right   = rect.x + rect.width;
            int top     = rect.y;
            int bottom  = rect.y + rect.height;

            logger.info(String.format("Element rect: x=%d y=%d w=%d h=%d", rect.x, rect.y, rect.width, rect.height));

            int[] coords = resolveCoordinates(swipeDirection, left, right, top, bottom, centerX, centerY);
            if (coords == null) {
                setErrorMessage("Unsupported direction: " + swipeDirection);
                return Result.FAILED;
            }

            int startX = coords[0];
            int startY = coords[1];
            int endX   = coords[2];
            int endY   = coords[3];

            logger.info(String.format("Swipe [%s]: (%d,%d) -> (%d,%d) over %ds",
                    swipeDirection, startX, startY, endX, endY, durationSeconds));

            AppiumDriver appiumDriver = (AppiumDriver) this.driver;
            PointerInput finger = new PointerInput(TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1)
                    .addAction(finger.createPointerMove(Duration.ofMillis(0), viewport(), startX, startY))
                    .addAction(finger.createPointerDown(LEFT.asArg()))
                    .addAction(finger.createPointerMove(Duration.ofSeconds(durationSeconds), viewport(), endX, endY))
                    .addAction(finger.createPointerUp(LEFT.asArg()));

            appiumDriver.perform(Collections.singletonList(swipe));

            setSuccessMessage(String.format(
                    "Successfully scrolled inside the element [%s] over %d second(s)", swipeDirection, durationSeconds));
            return Result.SUCCESS;

        } catch (NoSuchElementException e) {
            logger.debug("Element not found: " + e.getMessage());
            setErrorMessage("Element not found: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Scroll failed: " + e.getMessage());
            setErrorMessage("Scroll failed: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private int[] resolveCoordinates(String dir, int left, int right, int top, int bottom, int cx, int cy) {
        switch (dir) {
            // ── Horizontal ──────────────────────────────────────────────────────────
            case "LEFT TO RIGHT":   return new int[]{left,  cy, right, cy};
            case "RIGHT TO LEFT":   return new int[]{right, cy, left,  cy};
            case "MIDDLE TO LEFT":  return new int[]{cx,    cy, left,  cy};
            case "MIDDLE TO RIGHT": return new int[]{cx,    cy, right, cy};
            case "LEFT TO MIDDLE":  return new int[]{left,  cy, cx,    cy};
            case "RIGHT TO MIDDLE": return new int[]{right, cy, cx,    cy};
            // ── Vertical ────────────────────────────────────────────────────────────
            case "TOP TO BOTTOM":   return new int[]{cx, top,    cx, bottom};
            case "BOTTOM TO TOP":   return new int[]{cx, bottom, cx, top};
            case "TOP TO MIDDLE":   return new int[]{cx, top,    cx, cy};
            case "MIDDLE TO TOP":   return new int[]{cx, cy,     cx, top};
            case "BOTTOM TO MIDDLE":return new int[]{cx, bottom, cx, cy};
            case "MIDDLE TO BOTTOM":return new int[]{cx, cy,     cx, bottom};
            default:                return null;
        }
    }
}
