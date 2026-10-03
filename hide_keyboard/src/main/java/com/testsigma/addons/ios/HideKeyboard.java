package com.testsigma.addons.ios;

import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Hide the iOS keyboard",
        description = "Hides the on-screen keyboard on an iOS device.",
        applicationType = com.testsigma.sdk.ApplicationType.IOS)
public class HideKeyboard extends IOSAction {

    private static final String SUCCESS_MESSAGE = "Hide Keyboard executed successfully.";
    private static final String FAILURE_MESSAGE = "Unable to hide keyboard. Please try executing \"Tap on element\" outside keyboard.";

    @Override
    public Result execute() {
        logger.info("Attempting to hide the iOS keyboard");
        boolean keyboardShown = true;
        for (int i = 0; i < 4; i++) {
            long start = System.currentTimeMillis();
            if (i == 0) {
                switchToActiveElementAndPressEnter();
            } else if (i == 1) {
                hideKeyboardByTappingOutsideKeyboard();
            } else if (i == 2) {
                clickOnReturnKeys();
            } else {
                clickOnHideKeyBoardAccessibilityID();
            }
            logger.info("Hiding keyboard using strategy " + i + " took " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
            if (!isKeyboardShown()) {
                keyboardShown = false;
                break;
            }
        }
        if (!keyboardShown) {
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        } else {
            setErrorMessage(FAILURE_MESSAGE);
            return Result.FAILED;
        }
    }

    private void switchToActiveElementAndPressEnter() {
        try {
            driver.switchTo().activeElement().sendKeys(Keys.RETURN);
            logger.info("Hid keyboard by switching to active element and pressing Enter");
        } catch (Exception e) {
            logger.info("Could not hide keyboard by switching to active element and pressing Enter: " + e.getMessage());
        }
    }

    private void hideKeyboardByTappingOutsideKeyboard() {
        String keyboardClassName = "XCUIElementTypeKeyboard";
        logger.info("Trying to hide keyboard by tapping above keyboard element (class: " + keyboardClassName + ")");
        try {
            WebElement keyboard = driver.findElement(By.className(keyboardClassName));
            Point loc = keyboard.getLocation();
            PointerInput finger = new PointerInput(TOUCH, "finger");
            Sequence tap = new Sequence(finger, 1)
                    .addAction(finger.createPointerMove(ofMillis(0), viewport(), loc.getX() + 2, loc.getY() - 2))
                    .addAction(finger.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(finger, ofMillis(1)))
                    .addAction(finger.createPointerUp(LEFT.asArg()));
            ((AppiumDriver) driver).perform(Arrays.asList(tap));
            try { Thread.sleep(200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            logger.info("Tapped above keyboard successfully");
        } catch (Exception e) {
            logger.info("Failed to hide keyboard by tapping above keyboard: " + e.getMessage());
        }
    }

    private void clickOnReturnKeys() {
        logger.info("Trying to hide keyboard by clicking Return/Done/Search/Next/Go keys");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        List.of("Return", "return", "done", "Done", "search", "Search", "Next", "next", "Go", "go").forEach(button -> {
            try {
                driver.findElement(By.xpath("//*[contains(@name, '" + button + "')]")).click();
            } catch (Exception e) {
                logger.info("XPath click failed for key '" + button + "': " + e.getMessage());
            }
            try {
                driver.findElement(AppiumBy.name(button)).click();
            } catch (Exception e) {
                logger.info("Name lookup failed for key '" + button + "': " + e.getMessage());
            }
        });
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    private void clickOnHideKeyBoardAccessibilityID() {
        logger.info("Trying to hide keyboard via 'Hide keyboard' accessibility ID");
        try {
            driver.findElement(AppiumBy.accessibilityId("Hide keyboard")).click();
            logger.info("Clicked 'Hide keyboard' accessibility ID successfully");
        } catch (Exception e) {
            logger.info("Failed to click 'Hide keyboard' accessibility ID: " + e.getMessage());
        }
    }

    private boolean isKeyboardShown() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
            boolean shown = ((IOSDriver) driver).isKeyboardShown();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
            return shown;
        } catch (Exception e) {
            logger.info("Exception while checking keyboard visibility: " + e.getMessage());
            return false;
        }
    }
}
