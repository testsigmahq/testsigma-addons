package com.testsigma.addons.mobile_gestures.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;

import java.time.Duration;

@Data
@Action(actionText = "Flick Mobile screen to direction UP/DOWN/RIGHT/LEFT", applicationType = ApplicationType.IOS)
public class MobileFlick extends IOSAction {


    @TestData(reference = "UP/DOWN/RIGHT/LEFT", allowedValues = {"UP", "DOWN", "RIGHT", "LEFT"})
    private com.testsigma.sdk.TestData testData;

    String direction = testData.getValue().toString();

    /*
    @Author- Amit
    Whats Inside?
    Flick gesture based on Direction.You need to provide direction and based on the direction we pick up coordinates and use touch action to do the flick.
     */

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug(", test-data: " + this.testData.getValue());
        if (validateDirection()) {
            final TouchAction action = new TouchAction((PerformsTouchActions) driver);
            action.press(getStartPoint()).waitAction(WaitOptions.waitOptions(Duration.ofMillis(200L))).moveTo(getEndPoint()).release().perform();
            setSuccessMessage("Successfully Flicked in the direction" + direction);
            System.out.println("Successfully Flicked in the direction" + direction);

        } else {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(String.format("Operation Unsuccessful"));

        }
        return result;
    }

    private boolean validateDirection() {
        return this.direction.equalsIgnoreCase("UP") || this.direction.equalsIgnoreCase("DOWN") || this.direction.equalsIgnoreCase("RIGHT") || this.direction.equalsIgnoreCase("LEFT");
    }

    private PointOption getStartPoint() {
        final Dimension screenSize = driver.manage().window().getSize();
        if (this.direction.equalsIgnoreCase("UP")) {
            return PointOption.point(screenSize.width / 2, (int) (screenSize.height * 0.8));
        }
        if (this.direction.equalsIgnoreCase("DOWN")) {
            return PointOption.point(screenSize.width / 2, (int) (screenSize.height * 0.2));
        }
        if (this.direction.equalsIgnoreCase("RIGHT")) {
            return PointOption.point((int) (screenSize.width * 0.2), screenSize.height / 2);
        }
        return PointOption.point((int) (screenSize.width * 0.8), screenSize.height / 2);
    }

    private PointOption getEndPoint() {
        final Dimension screenSize = driver.manage().window().getSize();
        if (this.direction.equalsIgnoreCase("UP")) {
            return PointOption.point(screenSize.width / 2, (int) (screenSize.height * 0.2));
        }
        if (this.direction.equalsIgnoreCase("DOWN")) {
            return PointOption.point(screenSize.width / 2, (int) (screenSize.height * 0.8));
        }
        if (this.direction.equalsIgnoreCase("RIGHT")) {
            return PointOption.point((int) (screenSize.width * 0.8), screenSize.height / 2);
        }
        return PointOption.point((int) (screenSize.width * 0.2), screenSize.height / 2);
    }
}

