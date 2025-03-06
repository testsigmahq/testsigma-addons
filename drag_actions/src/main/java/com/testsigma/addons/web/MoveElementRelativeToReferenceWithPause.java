package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;


@Action(actionText = "Drag element elementLocator to the position of the element referenceElement with " +
        "offset x: xOffset , y: yOffset (with pause)",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class MoveElementRelativeToReferenceWithPause extends WebAction {

    @Element(reference = "elementLocator")
    private com.testsigma.sdk.Element dragElement;

    @Element(reference = "referenceElement")
    private com.testsigma.sdk.Element dropElement;

    @TestData(reference = "xOffset")
    private com.testsigma.sdk.TestData xOffsetData;

    @TestData(reference = "yOffset")
    private com.testsigma.sdk.TestData yOffsetData;

    @TestData(reference = "position", allowedValues = {"right", "left", "top", "bottom", "center"})
    private com.testsigma.sdk.TestData relativePositionData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Execution started");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        Point targetPoint = null;
        try {
            int xOffset = Integer.parseInt(xOffsetData.getValue().toString());
            int yOffset = Integer.parseInt(yOffsetData.getValue().toString());
            String relativePosition = relativePositionData.getValue().toString().toLowerCase();

            WebElement dragWebElement = driver.findElement(dragElement.getBy());
            WebElement dropWebElement = driver.findElement(dropElement.getBy());


            scrollToElement(dropWebElement);

            try {
                this.restStepWait(1);
            } catch (Exception e) {
                logger.info("Error occurred while waiting for element " + ExceptionUtils.getStackTrace(e));
                setErrorMessage("Error occurred while waiting for element " + ExceptionUtils.getStackTrace(e));
                result = com.testsigma.sdk.Result.FAILED;
            }
            logger.info("waited for 1 second before calculating drop location");

            Point startPoint = getCenterPoint(driver.findElement(dragElement.getBy()));
            targetPoint = calculateTargetPoint(driver.findElement(dropElement.getBy()),
                    relativePosition, xOffset, yOffset);

            logger.info("Start Point: " + startPoint + " Target Point: " + targetPoint);

            performDragAndDrop(dragWebElement, startPoint, targetPoint);
            logger.info("performed drag and drop");
            setSuccessMessage("Moved element to the " + relativePosition + " of the reference element");
        } catch (MoveTargetOutOfBoundsException me) {
            logger.debug("Move target out of bounds for values : " + targetPoint);
            setErrorMessage("Move target out of bounds for : " + targetPoint);
            result = com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Error during execution: " + ExceptionUtils.getStackTrace(e));
            logger.info(ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;

    }

    private Point getCenterPoint(WebElement element) {
        Map<String, Object> rect = getBoundingClientRect(element);
        int x = ((Number) rect.get("x")).intValue() + ((Number) rect.get("width")).intValue() / 2;
        int y = ((Number) rect.get("y")).intValue() + ((Number) rect.get("height")).intValue() / 2;
        return new Point(x, y);
    }

    private Point calculateTargetPoint(WebElement reference, String relativePosition, int xOffset, int yOffset) {
        Map<String, Object> rect = getBoundingClientRect(reference);
        int x = ((Number) rect.get("x")).intValue();
        int y = ((Number) rect.get("y")).intValue();
        int width = ((Number) rect.get("width")).intValue();
        int height = ((Number) rect.get("height")).intValue();

        switch (relativePosition) {
            case "center":
                x += width / 2;
                y += height / 2;
                break;
            case "right":
                x += width;
                y += height / 2;
                break;
            case "left":
                y += height / 2;
                break;
            case "top":
                x += width / 2;
                break;
            case "bottom":
                x += width / 2;
                y += height;
                break;
            default:
                throw new IllegalArgumentException("Invalid relative direction: " + relativePosition);
        }

        return new Point(x + xOffset, y + yOffset);
    }

    private void performDragAndDrop(WebElement source, Point start, Point target) {
        PointerInput mouse = new PointerInput(PointerInput.Kind.MOUSE, "mouse");
        Sequence dragAndDrop = new Sequence(mouse, 1);

        dragAndDrop.addAction(mouse.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), start.x, start.y));
        dragAndDrop.addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        dragAndDrop.addAction(mouse.createPointerMove(Duration.ofMillis(500),
                PointerInput.Origin.viewport(), target.x, target.y));
        dragAndDrop.addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        ((RemoteWebDriver) driver).perform(Arrays.asList(dragAndDrop));
    }

    private Map<String, Object> getBoundingClientRect(WebElement element) {
        return (Map<String, Object>) ((JavascriptExecutor) driver).executeScript(
                "var rect = arguments[0].getBoundingClientRect(); return {x: rect.x, y: rect.y, width: rect.width, height: rect.height};",
                element);
    }

    protected void scrollToElement(WebElement element) {
        String scrollToElement = "try{ "
                + "arguments[0].scrollIntoView({"
                + " behavior: 'auto', block: 'center', inline: 'center'"
                + "}); return false;"
                + "}catch(e){"
                + "return true;"
                + "}";
        Object result = ((JavascriptExecutor) driver).executeScript(scrollToElement, element);

        if (result instanceof Boolean && (Boolean) result) {
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, "
                    + "window.innerHeight || 0);"
                    + "var elementTop = arguments[0].getBoundingClientRect().top;"
                    + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

            ((JavascriptExecutor) driver).executeScript(scrollElementIntoMiddle, element);
        }
    }

    private void restStepWait(Integer waitInSeconds) {
        synchronized (this) {
            try {
                this.wait((waitInSeconds * 1000) - 10);
            } catch (Exception e) {
                logger.info(ExceptionUtils.getStackTrace(e));
                setErrorMessage("Unable to minimize window. Error: " + ExceptionUtils.getStackTrace(e));
            }
        }
    }


}

