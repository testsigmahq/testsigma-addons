package com.testsigma.addons.mobileWeb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.NoSuchElementException;

@Action(actionText = "Swipe in direction direction within containerElement until targetElement is visible",
        description = "Swipes within a container element in the given direction (left->right or top->bottom) until the target element becomes visible.",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class SwipeUntilElementVisible extends WebAction {

    @Element(reference = "containerElement")
    private com.testsigma.sdk.Element containerElement;

    @Element(reference = "targetElement")
    private com.testsigma.sdk.Element targetElement;

    @TestData(reference = "direction", allowedValues = {"left->right", "top->bottom"})
    private com.testsigma.sdk.TestData directionData;

    private static final int MAX_SWIPES = 20;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            logger.info("Execution started");
            String direction = directionData.getValue().toString().toLowerCase();
            logger.info("Swipe direction: " + direction);

            for (int attempt = 1; attempt <= MAX_SWIPES; attempt++) {
                if (isTargetVisible()) {
                    setSuccessMessage("Target element is visible after " + attempt + " swipe(s) in direction: " + direction);
                    return Result.SUCCESS;
                }
                logger.info("Swipe attempt " + attempt + " of " + MAX_SWIPES);
                performSwipe(direction);
            }

            if (isTargetVisible()) {
                setSuccessMessage("Target element is visible after " + MAX_SWIPES + " swipe(s) in direction: " + direction);
                return Result.SUCCESS;
            }

            setErrorMessage("Target element not visible after " + MAX_SWIPES + " swipe(s) in direction: " + direction);
            return Result.FAILED;
        } catch (NoSuchElementException e) {
            setErrorMessage("Element not found: " + e.getMessage());
            logger.info("Element not found: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Error: " + e.getMessage());
            logger.info("Error: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private boolean isTargetVisible() {
        try {
            WebElement target = targetElement.getElement();
            return target.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void performSwipe(String direction) {
        WebElement container = containerElement.getElement();
        Point location = container.getLocation();
        int width = container.getSize().getWidth();
        int height = container.getSize().getHeight();
        int cx = location.getX();
        int cy = location.getY();

        Point start;
        Point end;

        if ("left->right".equals(direction)) {
            start = new Point(cx + width - 10, cy + height / 2);
            end   = new Point(cx + 10,         cy + height / 2);
        } else {
            start = new Point(cx + width / 2, cy + height - 10);
            end   = new Point(cx + width / 2, cy + 10);
        }

        new Actions(driver)
                .moveToLocation(start.getX(), start.getY())
                .clickAndHold()
                .moveToLocation(end.getX(), end.getY())
                .release()
                .perform();
    }
}
