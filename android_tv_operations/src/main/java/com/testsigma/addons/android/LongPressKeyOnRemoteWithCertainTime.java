package com.testsigma.addons.android;

import com.google.common.collect.ImmutableMap;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Long press Key key-value for testdata seconds on TV Remote",
        description = "Long press the specified key on the Android TV remote for the given duration in seconds",
        applicationType = ApplicationType.ANDROID)
public class LongPressKeyOnRemoteWithCertainTime extends AndroidAction {

    @TestData(
            reference = "key-value",
            allowedValues = {
                    "Ok", "Up", "Down", "Left", "Right",
                    "Rewind", "Forward", "Ch+", "Ch-",
                    "Volume-Up", "Volume-Down"
            }
    )
    private com.testsigma.sdk.TestData key;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData time;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;

        try {
            // Validate input
            Integer timeInSeconds = Integer.parseInt(time.getValue().toString());
            if (timeInSeconds <= 0) {
                setErrorMessage("Invalid duration. Please specify a positive number of seconds.");
                return Result.FAILED;
            }

            logger.info("Initiating long press execution");
            AndroidDriver androidDriver = (AndroidDriver) this.driver;

            // Map the key name to the corresponding AndroidKey
            AndroidKey androidKey = KeyUtil.getKey(key.getValue().toString());
            logger.info("Long pressing key: " + key.getValue().toString() + " for " + timeInSeconds + " seconds.");

            // Press and hold the key
            androidDriver.executeScript("mobile: keyevent", ImmutableMap.of(
                    "keycode", androidKey.getCode(),
                    "eventType", "down" // Simulates pressing the key down
            ));

            // Wait for the specified duration
            Thread.sleep(timeInSeconds * 1000);

            // Release the key
            androidDriver.executeScript("mobile: keyevent", ImmutableMap.of(
                    "keycode", androidKey.getCode(),
                    "eventType", "up" // Simulates releasing the key
            ));

            setSuccessMessage("Successfully long pressed the key '" + key.getValue().toString() + "' for " + timeInSeconds + " seconds.");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid key value: " + key.getValue());
            logger.warn("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Invalid key value provided: " + key.getValue());
        } catch (Exception e) {
            logger.warn("Something went wrong during the long press action");
            logger.warn("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Failed to long press the key due to an error.");
        }
        return result;
    }
}
