package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Swipe on screen from bottom to top when the application has a bottom header",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class SwipeonScreenTest extends AndroidAction {



    @Override
    protected Result execute() throws NoSuchElementException {
        AndroidDriver anddriver = (AndroidDriver) driver;
        Dimension size = anddriver.manage().window().getSize();
        int startx = (int) (size.width * 0.50);
        int starty = (int) (size.height * 0.70);
        int endy = (int) (size.height * 0.10);
        PointerInput pointer = new PointerInput(TOUCH, "finger");
        Sequence swipe = new Sequence(pointer, 1)
                .addAction(pointer.createPointerMove(ofMillis(0), viewport(), startx, starty))
                .addAction(pointer.createPointerDown(LEFT.asArg()))
                .addAction(pointer.createPointerMove(ofMillis(600), viewport(), startx, endy))
                .addAction(pointer.createPointerUp(LEFT.asArg()));
        anddriver.perform(Arrays.asList(swipe));
        return Result.SUCCESS;
    }
}
