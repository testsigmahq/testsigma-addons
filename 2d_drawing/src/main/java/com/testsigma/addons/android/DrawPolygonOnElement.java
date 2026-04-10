package com.testsigma.addons.android;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.TapOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.util.List;

@Data
@Action(actionText = "Drawing: Draw a polygon on element element-locator using the list of relative positions data-points (Percentage of " +
        "element width, Percentage of element height. Ex: 5,10:15,20:20,25)",
        description = "Drawing polygon on element using the list of relative positions which are based on the element height and width",
        applicationType = ApplicationType.ANDROID)
public class DrawPolygonOnElement extends DrawingAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element camera;
    @TestData(reference = "data-points")
    private com.testsigma.sdk.TestData points_;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        String pointString = points_.getValue().toString();
        String[] pointStringList = pointString.split(":");
        try {
            elementRect = getCamera().getElement().getRect();
            logger.info(String.format("Element dimensions x: %s, y:%s, width:%s, height:%s",
                    elementRect.x, elementRect.y, elementRect.width, elementRect.height));
            List<Point> xyPairs = retrieveRelativePoints(pointStringList);
            if (xyPairs.size() < 2) {
                setRuntimeErrorMessage(String.format("Provided positions are %s, minimum 3 positions are" +
                        " required to draw polygon", xyPairs.size()));
                throw new RuntimeException("Invalid no of positions");
            }
            logger.info("Adding first point again to the last to complete the polygon drawing");
            xyPairs.add(xyPairs.get(0));

            AndroidDriver androidDriver = (AndroidDriver) driver;
            for (Point point : xyPairs) {
                new TouchAction<>(androidDriver)
                        .tap(TapOptions.tapOptions().withPosition(PointOption.point(point.x, point.y)))
                        .perform();
            }
            setSuccessMessage("Successfully drawn the polygon using the given positions on the element");
        } catch (RuntimeException e) {
            logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(runtimeErrorMessage);
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception raised :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to generate the polygon");
            result = Result.FAILED;
        }
        return result;
    }
}
