package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.touch.TapOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;

@Data
@Action(actionText = "Drawing: Draw a line on the element element-locator using start position (x1, y1) " +
        "and end position (x2, y2) (Position Format: Percentage of element width,Percentage of element height," +
        " Ex: 40,60)",
        description = "Drawing a line in the given element with given relative start and end positions",
        applicationType = ApplicationType.IOS)
public class DrawLineOnElement extends DrawingAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element camera;
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
            elementRect = getCamera().getElement().getRect();
            logger.info(String.format("Element dimensions x: %s, y:%s, width:%s, height:%s",
                    elementRect.x, elementRect.y, elementRect.width, elementRect.height));

            Point start = getPointFromString(x1.getValue().toString(), y1.getValue().toString());
            logger.info("Start point: " + start.toString());
            Point end = getPointFromString(x2.getValue().toString(), y2.getValue().toString());
            logger.info("End point: " + end.toString());

            IOSDriver iosDriver = (IOSDriver) driver;
            new TouchAction<>(iosDriver)
                    .tap(TapOptions.tapOptions().withPosition(PointOption.point(start.x, start.y)))
                    .perform();
            new TouchAction<>(iosDriver)
                    .tap(TapOptions.tapOptions().withPosition(PointOption.point(end.x, end.y)))
                    .perform();

            setSuccessMessage("Successfully drawn the line on the element");
        } catch (RuntimeException e) {
            logger.info("Exception occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(runtimeErrorMessage);
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception raised :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to draw the line");
            result = Result.FAILED;
        }
        return result;
    }
}
