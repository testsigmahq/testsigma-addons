package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

@Data
@Action(actionText = "Double click on testdata using x,y coordinates",
        description = "Double click on the web page at the specified X and Y coordinates, provided as a comma-separated string.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class DoubleClickOnCoordinates extends WebAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData coordinates;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        logger.debug("Coordinates: " + coordinates);
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String coordinates_= coordinates.getValue().toString();

        try {
            String[] parts = coordinates_.split(",");

            if (parts.length != 2) {
                result = com.testsigma.sdk.Result.FAILED;
                logger.warn("Invalid coordinates format.  Provide coordinates as 'x,y'.");
                setErrorMessage("Invalid coordinates format. Provide coordinates as 'x,y'.  Example: 100,200");
                return result;
            }

            int xCoordinate = Integer.parseInt(parts[0].trim());
            int yCoordinate = Integer.parseInt(parts[1].trim());

            Actions actions = new Actions(driver);
            actions.moveByOffset(xCoordinate, yCoordinate).doubleClick().perform();

            setSuccessMessage("Successfully double clicked at coordinates X: " + xCoordinate + ", Y: " + yCoordinate);

        } catch (NumberFormatException e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Invalid number format in coordinates. X and Y must be integers.");
            setErrorMessage("Invalid number format in coordinates. X and Y must be integers. Error: " + e.getMessage());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Error occurred while double clicking at coordinates: " + e.getMessage());
            setErrorMessage("Error occurred while double clicking at coordinates: " + coordinates + ". Error: " + e.getMessage());
        }

        return result;
    }
}