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
@Action(actionText = "Press Key key-value in TV Remote",
        description = "Press the given key in the android tv remote",
        applicationType = ApplicationType.ANDROID)
public class PressKeyOnRemote extends AndroidAction {

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

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            logger.info("Initiating execution");
            AndroidDriver androidDriver = (AndroidDriver)this.driver;
            AndroidKey androidKey = this.getKey(key.getValue().toString());
            androidDriver.pressKey(new KeyEvent(androidKey));
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

    private AndroidKey getKey(String value) {
        AndroidKey androidKey;
        switch (value) {
            case "Up":
                androidKey = AndroidKey.DPAD_UP;
                break;
            case "Down":
                androidKey = AndroidKey.DPAD_DOWN;
                break;
            case "Left":
                androidKey = AndroidKey.DPAD_LEFT;
                break;
            case "Right":
                androidKey = AndroidKey.DPAD_RIGHT;
                break;
            case "Guide":
                androidKey = AndroidKey.GUIDE;
                break;
            case "Tv-contents-menu":
                androidKey = AndroidKey.TV_CONTENTS_MENU;
                break;
            case "Menu":
                androidKey = AndroidKey.MENU;
                break;
            case "Info":
                androidKey = AndroidKey.INFO;
                break;
            case "Ch+":
                androidKey = AndroidKey.CHANNEL_UP;
                break;
            case "Ch-":
                androidKey = AndroidKey.CHANNEL_DOWN;
                break;
            case "Back":
                androidKey = AndroidKey.BACK;
                break;
            case "Ok":
                androidKey = AndroidKey.DPAD_CENTER;
                break;
            case "NUM_0":
                androidKey = AndroidKey.DIGIT_0;
                break;
            case "NUM_1":
                androidKey = AndroidKey.DIGIT_1;
                break;
            case "NUM_2":
                androidKey = AndroidKey.DIGIT_2;
                break;
            case "NUM_3":
                androidKey = AndroidKey.DIGIT_3;
                break;
            case "NUM_4":
                androidKey = AndroidKey.DIGIT_4;
                break;
            case "NUM_5":
                androidKey = AndroidKey.DIGIT_5;
                break;
            case "NUM_6":
                androidKey = AndroidKey.DIGIT_6;
                break;
            case "NUM_7":
                androidKey = AndroidKey.DIGIT_7;
                break;
            case "NUM_8":
                androidKey = AndroidKey.DIGIT_8;
                break;
            case "NUM_9":
                androidKey = AndroidKey.DIGIT_9;
                break;
            case "Volume-Up":
                androidKey = AndroidKey.VOLUME_UP;
                break;
            case "Volume-Down":
                androidKey = AndroidKey.VOLUME_DOWN;
                break;
            case "PlayPause":
                androidKey = AndroidKey.MEDIA_PLAY_PAUSE;
                break;
            case "Rewind":
                androidKey = AndroidKey.MEDIA_REWIND;
                break;
            case "Forward":
                androidKey = AndroidKey.MEDIA_FAST_FORWARD;
                break;
            case "Home":
                androidKey = AndroidKey.HOME;
                break;
            case "Power":
                androidKey = AndroidKey.POWER;
                break;
            case "App-switch":
                androidKey = AndroidKey.APP_SWITCH;
                break;
            case "Soft-left":
                androidKey = AndroidKey.SOFT_LEFT;
                break;
            case "Soft-right":
                androidKey = AndroidKey.SOFT_RIGHT;
                break;
            case "Navigate-previous":
                androidKey = AndroidKey.NAVIGATE_PREVIOUS;
                break;
            case "Navigate-next":
                androidKey = AndroidKey.NAVIGATE_NEXT;
                break;
            case "Navigate-in":
                androidKey = AndroidKey.NAVIGATE_IN;
                break;
            case "Navigate-out":
                androidKey = AndroidKey.NAVIGATE_OUT;
                break;
            case "Stem-1-Netflix":
                androidKey = AndroidKey.STEM_1;
                break;
            case "Stem-2":
                androidKey = AndroidKey.STEM_2;
                break;
            case "Stem-3":
                androidKey = AndroidKey.STEM_3;
                break;

            default:
                throw new IllegalArgumentException("Invalid key value");
        }

        return androidKey;
    }


}