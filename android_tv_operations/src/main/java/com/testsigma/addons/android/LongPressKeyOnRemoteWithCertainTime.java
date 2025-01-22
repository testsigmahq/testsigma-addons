package com.testsigma.addons.android;

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
import java.time.Duration;


@Data
@Action(actionText = "Long press Key key-value for testdata seconds on TV Remote",
        description = "Long press the specified key on the Android TV remote for the given duration in seconds",
        applicationType = ApplicationType.ANDROID)
public class LongPressKeyOnRemoteWithCertainTime extends AndroidAction {

    @TestData(
            reference = "key-value",
            allowedValues =
                    {
                            "Ok","Up","Down","Left","Right",
                            "Rewind","Forward","Ch+","Ch-",
                            "Volume-Up","Volume-Down"
                    }
    )
    private com.testsigma.sdk.TestData key;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData durationInSeconds;


    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            logger.info("Initiating execution");
            AndroidDriver androidDriver = (AndroidDriver)this.driver;
            AndroidKey androidKey = KeyUtil.getKey(key.getValue().toString());

            int duration = Integer.parseInt(durationInSeconds.getValue().toString());
            Duration pressDuration = Duration.ofSeconds(duration);
            KeyEvent keyEvent = new KeyEvent(androidKey);

            // Press the key down
            androidDriver.pressKey(keyEvent);
            // wait for a while
            Thread.sleep(pressDuration.toMillis());
            //Release the Key, create a new keyEvent for the release
            KeyEvent releaseKeyEvent = new KeyEvent(androidKey);
            androidDriver.pressKey(releaseKeyEvent);

            setSuccessMessage("Long pressed the key successfully for "+duration+" seconds");

        } catch (IllegalArgumentException e) {
            logger.info("Invalid key value or duration");
            logger.info("Error occurred: "+ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Invalid key value or duration");
        }
        catch (InterruptedException e) {
            logger.info("Thread was interrupted during the long press");
            logger.info("Error occurred: "+ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Thread Interrupted while pressing the key");

        }
        catch (Exception e) {
            logger.info("Something went wrong while pressing the given key");
            logger.info("Error occurred: "+ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Unable to press the given key");
        }
        return result;
    }
}
