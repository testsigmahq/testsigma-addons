package com.testsigma.addons.ios;


import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

@Action(actionText = "Drag element elementLocator to the relativePosition of the referenceElement with " +
        "offset x: xOffset , y: yOffset",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class MoveElementRelativeToReference extends AndroidAction {

    @Element(reference = "elementLocator")
    private com.testsigma.sdk.Element targetElement;

    @Element(reference = "referenceElement")
    private com.testsigma.sdk.Element referenceElement;

    @TestData(reference = "xOffset")
    private com.testsigma.sdk.TestData xOffsetData;

    @TestData(reference = "yOffset")
    private com.testsigma.sdk.TestData yOffsetData;

    @TestData(reference = "relativePosition", allowedValues = {"right", "left", "top", "bottom","center"})
    private com.testsigma.sdk.TestData relativePositionData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Execution started");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        int xOffset = Integer.parseInt(xOffsetData.getValue().toString());
        int yOffset = Integer.parseInt(yOffsetData.getValue().toString());
        logger.info("xOffset: " + xOffset + " yOffset: " + yOffset);
        String relativePosition = relativePositionData.getValue().toString().toLowerCase();
        logger.info("relativePosition: " + relativePosition);
        int targetXOffset=-1; //initializing values so that we can use in error message
        int targetYOffset=-1;
        try {
            WebElement referenceWebElement = referenceElement.getElement();
            WebElement targetWebElement = targetElement.getElement();

            targetXOffset = calculateXOffset(referenceWebElement, relativePosition, xOffset);
            targetYOffset = calculateYOffset(referenceWebElement, relativePosition, yOffset);
            logger.info("Target X Offset: " + targetXOffset + " Target Y Offset: " + targetYOffset);
            IOSDriver iosDriver = (IOSDriver) driver;
            Actions actions = new Actions(iosDriver);
            actions.moveToElement(targetWebElement)
                    .clickAndHold()
                    .moveToLocation(targetXOffset, targetYOffset)
                    .release().build().perform();
            logger.info("Moved element");
            setSuccessMessage("Moved element to the " + relativePosition + " of the reference element");
        } catch (NoSuchElementException e) {
            setErrorMessage("Element not found: " + e.getMessage());
            logger.info(ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        } catch (MoveTargetOutOfBoundsException e) {
            setErrorMessage("Target location" + targetXOffset +"," + targetYOffset +"is out of bounds: " + e.getMessage());
            logger.info(ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            setErrorMessage(e.getMessage());
            logger.info(ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
    private int calculateXOffset(WebElement referenceElement, String relativePosition, int xOffset) {
        int referenceX = referenceElement.getLocation().getX();
        int referenceWidth = referenceElement.getSize().getWidth();
        logger.info("referenceX: " + referenceX + " referenceWidth: " + referenceWidth);
        switch (relativePosition) {
            case "center":
                return referenceX + referenceWidth / 2 + xOffset;
            case "right":
                return referenceX + referenceWidth + xOffset;
            case "left":
                return referenceX - xOffset;
            default:
                return referenceX + xOffset; // Default for "top" and "bottom"
        }
    }

    private int calculateYOffset(WebElement referenceElement, String relativePosition, int yOffset) {
        int referenceY = referenceElement.getLocation().getY();
        int referenceHeight = referenceElement.getSize().getHeight();
        logger.info("referenceY: " + referenceY + " referenceHeight: " + referenceHeight);
        switch (relativePosition) {
            case "center":
                return referenceY + referenceHeight / 2 + yOffset;
            case "bottom":
                return referenceY + referenceHeight + yOffset;
            case "top":
                return referenceY - yOffset;
            default:
                return referenceY + yOffset; // Default for "right" and "left"
        }
    }
}
