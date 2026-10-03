package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

@Data
@Action(actionText = "Drawing: Draw a Rectangle on the element element-locator with dimensions Length: Test-Data1 ," +
        " Breadth: Test-Data2 and start position (x1, y1) (Position Format: Percentage of element width,Percentage" +
        " of element height, Ex: 40,60)",
        description = "Drawing a rectangle in the given element based on the given dimensions and position",
        applicationType = ApplicationType.WEB
        
    )
public class DrawRectangleOnElement extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element camera;
    @TestData(reference = "x1")
    private com.testsigma.sdk.TestData xPercentage;
    @TestData(reference = "y1")
    private com.testsigma.sdk.TestData yPercentage;
    @TestData(reference = "Test-Data1")
    private com.testsigma.sdk.TestData width;
    @TestData(reference = "Test-Data2")
    private com.testsigma.sdk.TestData height;


    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try{
            Rectangle rectangle = getCamera().getElement().getRect();
            logger.info(String.format("Element dimensions x: %s, y:%s, width:%s, height:%s",
                    rectangle.x, rectangle.y, rectangle.width, rectangle.height));
            double x = Double.parseDouble(xPercentage.getValue().toString());
            double y = Double.parseDouble(yPercentage.getValue().toString());
            int boxWidth = Integer.parseInt(width.getValue().toString());
            int boxHeight = Integer.parseInt(height.getValue().toString());
            int startX = (int) ((x * rectangle.getWidth() / 100) + rectangle.getX());
            int startY = (int) ((y * rectangle.getHeight() / 100) + rectangle.getY());
            int endX = startX + boxWidth;
            int endY = startY + boxHeight;
            logger.info("Start locations relative to the given element startX - " + startX + ", startY - " + startY);

            Actions actions = new Actions(driver);
            actions.moveToLocation(startX, startY).clickAndHold().moveToLocation(endX, endY).release().build().perform();

            setSuccessMessage(String.format("Successfully created the rectangle on the given location, started from" +
                    " x: %s, y:%s", startX, startY));
        } catch (MoveTargetOutOfBoundsException e) {
            logger.info("Invalid locations raised :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("The positions are not with in the element");
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception raised :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to generate the rectangle");
            result = Result.FAILED;
        }
        return result;

    }
}