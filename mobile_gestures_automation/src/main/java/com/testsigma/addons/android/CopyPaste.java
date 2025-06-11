package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Action(actionText = "Copy OR paste selected text. Action to be performed: Copy-Paste",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class CopyPaste extends AndroidAction {

/*  @TestData(reference = "Zoom-Action",allowedValues = {"Pinch","Zoom"})
  private com.testsigma.sdk.TestData zoomAction;*/

    @TestData(reference = "Copy-Paste",allowedValues = {"Copy","Paste"})
    private com.testsigma.sdk.TestData action;


    StringBuilder builder = new StringBuilder();


    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        AndroidDriver androidDriver = (AndroidDriver) driver;
        try{
            Actions a = new Actions(androidDriver);
               if(action.getValue().toString().equalsIgnoreCase("Copy")){
                   androidDriver.pressKey(new KeyEvent(AndroidKey.COPY));
               }else if(action.getValue().toString().equalsIgnoreCase("Paste")){
                   androidDriver.pressKey(new KeyEvent(AndroidKey.PASTE));
               }
               setSuccessMessage("Successfully performed Copy-Paste action");
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Error occurred:" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform action:" + e.getMessage() );
        }

        return result;
    }

}