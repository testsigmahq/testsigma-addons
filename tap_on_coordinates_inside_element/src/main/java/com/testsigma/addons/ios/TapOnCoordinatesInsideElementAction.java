package com.testsigma.addons.ios;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v135.io.IO;
import org.openqa.selenium.interactions.Actions;

@Action(actionText = "Tap on coordinates test-data(% of width, % of height from element top left) inside element element-locator",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class TapOnCoordinatesInsideElementAction extends IOSAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    private static final String SUCCESS_MESSAGE = "Successfully clicked on given location.<br>Element's Location(x,y): %s , %s<br>" +
            "Click Location(x,y): %s , %s";
    private static final String FAILURE_MESSAGE = "Unable to click at given location. Please verify if click action can be performed on given location." +
            "<br>Element's location(x,y): %s , %s<br>Click Location(x,y): %s , %s";
    private static final String FAILURE_NOT_A_NUMBER_X = "Please provide a valid width percentage (can be a decimal too) in test data, given width <b>%s</b> is not a number.";
    private static final String FAILURE_NOT_A_NUMBER_Y = "Please provide a valid height percentage (can be a decimal too) in test data, given height <b>%s</b> is not a number.";
    private static final String FAILURE_NO_SEPARATOR = "Please provide valid test data, given test data <b>\"%s\"</b> is not valid.";
    private static final String TEST_DATA_FORMAT = "Test data format: Percentage of width , percentage of height (Ex: 20,40)<br>" +
            "If the elements' dimensions are 1000 * 400, and if you want to click at position 200,100 (Assuming element's top left is at 0,0) " +
            "then the test data should be 20,25 (which means 20 percent of 1000 , 25 percent of 400).";

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        IOSDriver iosDriver = (IOSDriver) this.driver;
        WebElement webElement = element.getElement();

        try {
            String testDataValue = testData.getValue().toString();
            logger.info("Test data received: " + testDataValue);

            if (!testDataValue.contains(",")) {
                String msg = String.format(FAILURE_NO_SEPARATOR + "<br>" + TEST_DATA_FORMAT, testDataValue);
                logger.warn(msg);
                setErrorMessage(msg);
                return Result.FAILED;
            }

            String[] coordinates = testDataValue.trim().split(",");
            double xPercent, yPercent;

            try {
                xPercent = Double.parseDouble(coordinates[0].trim());
            } catch (NumberFormatException e) {
                String msg = String.format(FAILURE_NOT_A_NUMBER_X + "<br>" + TEST_DATA_FORMAT, coordinates[0]);
                logger.warn(msg);
                setErrorMessage(msg);
                return Result.FAILED;
            }

            try {
                yPercent = Double.parseDouble(coordinates[1].trim());
            } catch (NumberFormatException e) {
                String msg = String.format(FAILURE_NOT_A_NUMBER_Y + "<br>" + TEST_DATA_FORMAT, coordinates[1]);
                logger.warn(msg);
                setErrorMessage(msg);
                return Result.FAILED;
            }

            if (xPercent > 100 || yPercent > 100) {
                String msg = "";
                if (xPercent > 100) {
                    msg = String.format("Given width percentage <b>%s</b> is greater than 100.<br>" + TEST_DATA_FORMAT, coordinates[0]);
                } else {
                    msg = String.format("Given height percentage <b>%s</b> is greater than 100.<br>" + TEST_DATA_FORMAT, coordinates[1]);
                }
                logger.warn(msg);
                setErrorMessage(msg);
                return Result.FAILED;
            }

            Rectangle rect = webElement.getRect();
            int elementX = rect.getX();
            int elementY = rect.getY();
            int width = rect.getWidth();
            int height = rect.getHeight();

            logger.info(String.format("Element rect - X: %d, Y: %d, Width: %d, Height: %d", elementX, elementY, width, height));
            logger.info(String.format("Element bounds - Left: %d, Top: %d, Right: %d, Bottom: %d", 
                elementX, elementY, elementX + width, elementY + height));

            double clickLocationX = (xPercent * width / 100);
            double clickLocationY = (yPercent * height / 100);

            int clickX = elementX + (int) clickLocationX;
            int clickY = elementY + (int) clickLocationY;

            logger.info(String.format("Relative calculation - X: %f", clickLocationX));
            logger.info(String.format("Relative calculation - Y: %f", clickLocationY));
            logger.info(String.format("Final screen coordinates - X: %d" , clickX));
            logger.info(String.format("Final screen coordinates - Y: %d", clickY));

            logger.info(String.format("Clicking on element at screen coordinates: (%d, %d)", clickX, clickY));


            Actions actions = new Actions(iosDriver);
            actions.moveToElement(webElement, (int) clickLocationX, (int) clickLocationY).click().build().perform();

            logger.info("Click action performed successfully.");
            setSuccessMessage(String.format(SUCCESS_MESSAGE, elementX, elementY, clickX, clickY));

        } catch (Exception e) {
            logger.warn("Exception occurred while clicking on element: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(String.format(FAILURE_MESSAGE, 0, 0, 0, 0));
            result = Result.FAILED;
        }

        return result;
    }
}
