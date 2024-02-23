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

@Data
@Action(actionText = "Press Key key-value for test-data times in TV Remote with wait time test-data1 seconds between the key press",
        description = "Press the given key in the android tv remote with given no of times with given wait time between the key press",
        applicationType = ApplicationType.ANDROID)
public class PressKeyMultipleTimes extends AndroidAction {

    @TestData(
            reference = "key-value",
            allowedValues =
                    {
                            "Up", "Down", "Left", "Right", "Guide", "Tv-contents-menu",
                            "Menu", "Info", "Ch+", "Ch-", "Back", "Ok",
                            "NUM_0", "NUM_1", "NUM_2", "NUM_3", "NUM_4", "NUM_5",
                            "NUM_6", "NUM_7", "NUM_8", "NUM_9", "Volume-Up", "Volume-Down",
                            "PlayPause", "Rewind", "Forward", "Home", "Power", "App-switch",
                            "Soft-left", "Soft-right", "Navigate-previous", "Navigate-next", "Navigate-in", "Navigate-out",
                            "Stem-1-Netflix", "Stem-2", "Stem-3"
                    }
    )
    private com.testsigma.sdk.TestData key;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData testData1;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            logger.info("Initiating execution");
            AndroidDriver androidDriver = (AndroidDriver)this.driver;
            int noOfTimes = Integer.parseInt(testData.getValue().toString());
            int waitTime = Integer.parseInt(testData1.getValue().toString()) * 1000;
            logger.info("Wait time:" + waitTime + "milliseconds");
            AndroidKey androidKey = KeyUtil.getKey(key.getValue().toString());
            for (int i = 0; i < noOfTimes; i++) {
                androidDriver.pressKey(new KeyEvent(androidKey));
                logger.info("Press key event done");
                Thread.sleep(waitTime);
            }
            setSuccessMessage("Pressed the key successfully");
        } catch (IllegalArgumentException e) {
            logger.info("Invalid key value");
            logger.info("Error occurred: "+ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Invalid key value");
        } catch (Exception e) {
            logger.info("Something went wrong while pressing the given key");
            logger.info("Error occurred: "+ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            setErrorMessage("Unable to press the given key");
        }
        return result;
    }
}