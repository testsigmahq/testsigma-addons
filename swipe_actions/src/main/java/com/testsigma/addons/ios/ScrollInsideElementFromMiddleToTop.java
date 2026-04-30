package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Element;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Swipe inside the element element-name from middle to top",
        description = "Scrolls inside the given element starting from its vertical midpoint up to its top edge." +
                " Useful for revealing content above the centre of the element.",
        applicationType = ApplicationType.IOS,
        displayName = "Scroll inside element from middle to top",
        useCustomScreenshot = false)
public class ScrollInsideElementFromMiddleToTop extends IOSAction {

    @com.testsigma.sdk.annotation.Element(reference = "element-name")
    private Element elementName;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Starting scroll inside element from middle to top");

        try {
            org.openqa.selenium.WebElement webElement = elementName.getElement();
            Rectangle rect = webElement.getRect();

            int startX = rect.x + rect.width / 2;
            int startY = rect.y + rect.height / 2;
            int endX   = startX;
            int endY   = rect.y;

            logger.info(String.format("Element rect: x=%d y=%d w=%d h=%d", rect.x, rect.y, rect.width, rect.height));
            logger.info(String.format("Swipe: (%d,%d) -> (%d,%d)", startX, startY, endX, endY));

            AppiumDriver appiumDriver = (AppiumDriver) this.driver;
            PointerInput finger = new PointerInput(TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1)
                    .addAction(finger.createPointerMove(Duration.ofMillis(0), viewport(), startX, startY))
                    .addAction(finger.createPointerDown(LEFT.asArg()))
                    .addAction(finger.createPointerMove(Duration.ofSeconds(2), viewport(), endX, endY))
                    .addAction(finger.createPointerUp(LEFT.asArg()));

            appiumDriver.perform(Collections.singletonList(swipe));

            setSuccessMessage("Successfully scrolled inside the element from middle to top");
            return Result.SUCCESS;

        } catch (NoSuchElementException e) {
            logger.debug("Element not found: " + e.getMessage());
            setErrorMessage("Element not found: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            logger.debug("Failed to scroll inside element from middle to top: " + e.getMessage());
            setErrorMessage("Failed to scroll: " + e.getMessage());
            return Result.FAILED;
        }
    }
}
