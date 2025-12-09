package com.testsigma.addons.web;


import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.NoSuchElementException;

@Action(actionText = "Swipe from direction1 to direction2 within the element elementLocator",
        description = "Swipes from the given direction within the bounds of the specified element.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SwipeWithInElement extends WebAction {
    @TestData(reference = "direction1", allowedValues = {"up", "down", "left", "right"})
    private com.testsigma.sdk.TestData direction1Data;
    @TestData(reference = "direction2", allowedValues = {"up", "down", "left", "right"})
    private com.testsigma.sdk.TestData direction2Data;

    @Element(reference = "elementLocator")
    private com.testsigma.sdk.Element element;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            logger.info("Execution started");
            WebElement webElement = element.getElement();
            logger.info("Located element: " + element.getBy().toString());
            String firstDirection = direction1Data.getValue().toString().toLowerCase();
            String secondDirection = direction2Data.getValue().toString().toLowerCase();

            Point startLocation = getPointInElement(webElement, firstDirection);
            Point endLocation = getPointInElement(webElement, secondDirection);
            logger.info("Swiping from " + firstDirection + " to " + secondDirection +
                    " within element located by: " + element.getBy().toString());
            Actions actions = new Actions(driver);
            actions.moveToLocation(startLocation.getX(), startLocation.getY())
                    .clickAndHold()
                    .moveToLocation(endLocation.getX(), endLocation.getY())
                    .release()
                    .perform();
            setSuccessMessage("Successfully swiped from " + firstDirection + " to " + secondDirection +
                    " within the element located by: " + element.getBy().toString());
        } catch (NoSuchElementException e) {
            setErrorMessage("Element not found : " + e.getMessage());
            logger.info("Element not found : " + e.getMessage());
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Error : " + e.getMessage());
            setErrorMessage("Error : " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }

    private Point getPointInElement(WebElement element, String direction) {
        Point location = element.getLocation();
        int elementWidth = element.getSize().getWidth();
        int elementHeight = element.getSize().getHeight();
        int x = location.getX();
        int y = location.getY();

        switch (direction) {
            case "up":
                return new Point(x + elementWidth / 2, y + 10);
            case "down":
                return new Point(x + elementWidth / 2, y + elementHeight - 10);
            case "left":
                return new Point(x + 10, y + elementHeight / 2);
            case "right":
                return new Point(x + elementWidth - 10, y + elementHeight / 2);
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }
}
