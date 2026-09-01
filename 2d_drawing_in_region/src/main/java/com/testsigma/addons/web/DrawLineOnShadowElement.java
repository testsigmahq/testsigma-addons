package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Drawing: Draw a line on the shadow DOM element with host element-locator and shadow DOM CSS locator shadow-dom-css-locator using start position (x1, y1) and end position (x2, y2) (Position Format: Percentage of element width, Percentage of element height, Ex: 40,60)",
        description = "Drawing a line on the element inside Shadow DOM: use host element and shadow DOM CSS locator to find the target, then draw with given relative start and end positions",
        applicationType = ApplicationType.WEB)
public class DrawLineOnShadowElement extends DrawingAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element hostElement;
    @TestData(reference = "shadow-dom-css-locator")
    private com.testsigma.sdk.TestData shadowDomCssLocator;
    @TestData(reference = "x1")
    private com.testsigma.sdk.TestData x1;
    @TestData(reference = "y1")
    private com.testsigma.sdk.TestData y1;
    @TestData(reference = "x2")
    private com.testsigma.sdk.TestData x2;
    @TestData(reference = "y2")
    private com.testsigma.sdk.TestData y2;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            WebElement target = ShadowDomHelper.findElementInShadow(
                    getHostElement().getElement(),
                    shadowDomCssLocator.getValue().toString().trim());
            elementRect = target.getRect();
            logger.info(String.format("Shadow element dimensions x: %s, y:%s, width:%s, height:%s",
                    elementRect.x, elementRect.y, elementRect.width, elementRect.height));

            Point start = getPointFromString(x1.getValue().toString(), y1.getValue().toString());
            logger.info("Start point: " + start.toString());
            Point end = getPointFromString(x2.getValue().toString(), y2.getValue().toString());
            logger.info("End point: " + end.toString());
            Actions actions = new Actions(driver);
            actions.moveToLocation(start.x, start.y).click().moveToLocation(end.x, end.y).click().build().perform();

            setSuccessMessage("Successfully drawn the line on the shadow DOM element");
        } catch (MoveTargetOutOfBoundsException e) {
            logger.info("Invalid locations raised :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("The positions are not within the element");
            result = Result.FAILED;
        } catch (RuntimeException e) {
            logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(runtimeErrorMessage);
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception raised :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to draw the line on shadow DOM element");
            result = Result.FAILED;
        }
        return result;
    }
}
