package com.testsigma.addons.web;

import com.testsigma.addons.utils.DragActionUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;


@Action(actionText = "Drag element elementLocator to the position of the element referenceElement with " +
        "offset x: xOffset , y: yOffset (with multiple pauses)",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class MoveElementToReferenceWithMultiplePause extends WebAction {

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
            DragActionUtils dragActionUtils = new DragActionUtils(driver, logger);
            int xOffset = Integer.parseInt(xOffsetData.getValue().toString());
            int yOffset = Integer.parseInt(yOffsetData.getValue().toString());
            String relativePosition = relativePositionData.getValue().toString().toLowerCase();

            WebElement dragWebElement = driver.findElement(dragElement.getBy());
            WebElement dropWebElement = driver.findElement(dropElement.getBy());

            dragActionUtils.scrollToElement(dropWebElement);
            try {
                dragActionUtils.restStepWait(1);
            } catch (Exception e) {
                logger.info("Error occurred while waiting for element " + ExceptionUtils.getStackTrace(e));
                setErrorMessage("Error occurred while waiting for element " + ExceptionUtils.getStackTrace(e));
                result = com.testsigma.sdk.Result.FAILED;
            }
            logger.info("waited for 1 second before calculating drop location");

            Point startPoint = dragActionUtils.getCenterPoint(driver.findElement(dragElement.getBy()));
            targetPoint = dragActionUtils.calculateTargetPoint(driver.findElement(dropElement.getBy()),
                    relativePosition, xOffset, yOffset);

            logger.info("Start Point: " + startPoint + " Target Point: " + targetPoint);

            dragActionUtils.performDragAndDrop(dragWebElement, startPoint, targetPoint);
            logger.info("performed drag and drop");
            setSuccessMessage("Moved element to the " + relativePosition + " of the reference element");
        } catch (NoSuchElementException e) {
            setErrorMessage("Element not found: " + e.getMessage());
            logger.info(ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
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
}

