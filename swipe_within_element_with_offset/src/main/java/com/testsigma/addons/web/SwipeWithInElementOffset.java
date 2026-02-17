package com.testsigma.addons.web;

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

@Action(
        actionText = "Swipe direction within the element elementLocator",
        description = "Swipes in the given direction within the bounds of the specified element (e.g. TOP TO BOTTOM, TOP TO MIDDLE).",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class SwipeWithInElementOffset extends WebAction {

    @TestData(reference = "direction",
            allowedValues = {
                    "TOP TO BOTTOM", "BOTTOM TO TOP",
                    "TOP TO MIDDLE", "MIDDLE TO TOP",
                    "MIDDLE TO BOTTOM", "BOTTOM TO MIDDLE",
                    "LEFT TO RIGHT", "RIGHT TO LEFT",
                    "LEFT TO MIDDLE", "MIDDLE TO LEFT",
                    "MIDDLE TO RIGHT", "RIGHT TO MIDDLE"
            })
    private com.testsigma.sdk.TestData directionData;

    @Element(reference = "elementLocator")
    private com.testsigma.sdk.Element element;

    @Override
    public Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;

        try {
            logger.info("Execution started");

            WebElement webElement = element.getElement();
            logger.info("Located element: " + element.getBy());

            String direction = directionData.getValue().toString().trim().toUpperCase();
            String[] parts = direction.split(" TO ");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Direction must be in format 'X TO Y', e.g. TOP TO BOTTOM. Got: " + direction);
            }
            String fromPosition = parts[0].trim();
            String toPosition = parts[1].trim();

            Point startPoint = getPointInElement(webElement, fromPosition);
            Point endPoint = getPointInElement(webElement, toPosition);

            Actions actions = new Actions(driver);
            actions.moveToLocation(startPoint.getX(), startPoint.getY())
                    .clickAndHold()
                    .moveToLocation(endPoint.getX(), endPoint.getY())
                    .release()
                    .perform();

            setSuccessMessage("Successfully swiped " + direction +
                    " within element located by: " + element.getBy());

        } catch (NoSuchElementException e) {
            logger.info("Element not found: " + e.getMessage());
            setErrorMessage("Element not found: " + e.getMessage());
            result = Result.FAILED;

        } catch (Exception e) {
            logger.info("Error occurred: " + e.getMessage());
            setErrorMessage("Error: " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }


    private Point getPointInElement(WebElement element, String position) {
        Point location = element.getLocation();
        int elementWidth = element.getSize().getWidth();
        int elementHeight = element.getSize().getHeight();
        int x = location.getX();
        int y = location.getY();
        int inset = 10;

        switch (position) {
            case "TOP":
                return new Point(x + elementWidth / 2, y + inset);
            case "MIDDLE":
                return new Point(x + elementWidth / 2, y + elementHeight / 2);
            case "BOTTOM":
                return new Point(x + elementWidth / 2, y + elementHeight - inset);
            case "LEFT":
                return new Point(x + inset, y + elementHeight / 2);
            case "RIGHT":
                return new Point(x + elementWidth - inset, y + elementHeight / 2);
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }
}
