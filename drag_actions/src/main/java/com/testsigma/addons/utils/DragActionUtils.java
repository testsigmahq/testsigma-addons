package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

public class DragActionUtils {

    WebDriver driver;
    Logger logger;

    public DragActionUtils(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    public Point getCenterPoint(WebElement element) {
        Map<String, Object> rect = getBoundingClientRect(element);
        int x = ((Number) rect.get("x")).intValue() + ((Number) rect.get("width")).intValue() / 2;
        int y = ((Number) rect.get("y")).intValue() + ((Number) rect.get("height")).intValue() / 2;
        return new Point(x, y);
    }

    public Point calculateTargetPoint(WebElement reference, String relativePosition, int xOffset, int yOffset) {
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

    public void performDragAndDrop(WebElement source, Point start, Point target) {
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

    protected Map<String, Object> getBoundingClientRect(WebElement element) {
        return (Map<String, Object>) ((JavascriptExecutor) driver).executeScript(
                "var rect = arguments[0].getBoundingClientRect(); return {x: rect.x, y: rect.y, width: rect.width, height: rect.height};",
                element);
    }

    public void scrollToElement(WebElement element) {
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

    public void restStepWait(Integer waitInSeconds) {
        synchronized (this) {
            try {
                this.wait((waitInSeconds * 1000) - 10);
            } catch (Exception e) {
                logger.info(ExceptionUtils.getStackTrace(e));
                throw new RuntimeException("Unable to minimize window. Error: " + ExceptionUtils.getStackTrace(e));
            }
        }
    }

}
