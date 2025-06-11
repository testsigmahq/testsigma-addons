package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

@Action(actionText = "Open App switcher OR Open recent Apps",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class AppSwitcher extends AndroidAction {

/*  @TestData(reference = "Zoom-Action",allowedValues = {"Pinch","Zoom"})
  private com.testsigma.sdk.TestData zoomAction;*/




    StringBuilder builder = new StringBuilder();


    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        AndroidDriver androidDriver = (AndroidDriver) driver;
        try{
            androidDriver.pressKey(new KeyEvent(AndroidKey.APP_SWITCH));

            setSuccessMessage("Successfully performed action");
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Error occurred:" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform action:" + e.getMessage() );
        }

        return result;
    }

}