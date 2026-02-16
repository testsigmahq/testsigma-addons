package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

import java.awt.*;
import java.util.List;
import java.util.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Drawing: Draw a polygon on shadow DOM element with host element-locator and shadow DOM CSS locator shadow-dom-css-locator using the list of relative positions data-points (Percentage of element width, Percentage of element height. Ex: 5,10:15,20:20,25)",
        description = "Drawing polygon on element inside Shadow DOM: use host element and shadow DOM CSS locator to find the target, then draw using the list of relative positions",
        applicationType = ApplicationType.WEB)
public class DrawPolygonOnShadowElement extends DrawingAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element hostElement;
    @TestData(reference = "shadow-dom-css-locator")
    private com.testsigma.sdk.TestData shadowDomCssLocator;
    @TestData(reference = "data-points")
    private com.testsigma.sdk.TestData points_;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        String pointString = points_.getValue().toString();
        String[] pointStringList = pointString.split(":");
        try {
            WebElement target = ShadowDomHelper.findElementInShadow(
                    getHostElement().getElement(),
                    shadowDomCssLocator.getValue().toString().trim());
            elementRect = target.getRect();
            logger.info(String.format("Shadow element dimensions x: %s, y:%s, width:%s, height:%s",
                    elementRect.x, elementRect.y, elementRect.width, elementRect.height));
            List<Point> xyPairs = retrieveRelativePoints(pointStringList);
            if (xyPairs.size() < 2) {
                setRuntimeErrorMessage(String.format("Provided positions are %s, minimum 3 positions are required to draw polygon", xyPairs.size()));
                throw new RuntimeException("Invalid no of positions");
            }
            logger.info("Adding first point again to the last to complete the polygon drawing");
            xyPairs.add(xyPairs.get(0));
            Actions actions = new Actions(driver);
            for (Point point : xyPairs) {
                actions.moveToLocation(point.x, point.y).click().build().perform();
            }
            setSuccessMessage("Successfully drawn the polygon using the given positions on the shadow DOM element");
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
            setErrorMessage("Unable to generate the polygon on shadow DOM element");
            result = Result.FAILED;
        }
        return result;
    }
}
