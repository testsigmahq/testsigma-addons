package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class DrawingAction extends WebAction {

    protected org.openqa.selenium.Rectangle elementRect;
    protected String runtimeErrorMessage = "Unable to perform the operation, run time error occurred";

    @Override
    protected Result execute() throws NoSuchElementException {
        return null;
    }


    protected List<Point> retrieveRelativePoints(String[] pointsString) {
        List<Point> xyPairs = new ArrayList<>();
        for (String xy : pointsString) {
            logger.info("Converting to point: " + xy);
            String[] pointValues = xy.split(",");
            if (pointValues.length != 2) {
                setRuntimeErrorMessage("Invalid input positions given, it should be (Ex: x1,y1:x2,y2:x3,y3).");
                throw new RuntimeException("Invalid input");
            }
            Point point = getPointFromString(pointValues[0], pointValues[1]);
            logger.info(String.format("Point string %s, point : %s", xy, point.toString()));
            xyPairs.add(point);
        }
        return xyPairs;
    }

    protected Point getPointFromString(String xStr, String yStr) {
        try {
            logger.info("Retrieving relative x and y");
            double x = Double.parseDouble(xStr);
            double y = Double.parseDouble(yStr);
            int relativeX = (int) ((x * elementRect.getWidth() / 100) + elementRect.getX());
            int relativeY = (int) ((y * elementRect.getHeight() / 100) + elementRect.getY());
            logger.info("Retrieved..");
            return new Point(relativeX, relativeY);
        } catch (NumberFormatException e) {
            setRuntimeErrorMessage("Invalid input positions given, input should contain numbers in the format " +
                    "(Ex: x1,y1:x2,y2:x3,y3)");
            throw new RuntimeException("Invalid input, number conversion!!");
        }
    }
}
