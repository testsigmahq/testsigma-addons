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
@Action(actionText = "Long press Key key-value in TV Remote",
        description = "Long press the given key in the android tv remote",
        applicationType = ApplicationType.ANDROID)
public class LongPressKeyOnRemote extends AndroidAction {

    @TestData(
            reference = "key-value",
            allowedValues =
                    {
                            "Ok"
                    }
    )
    private com.testsigma.sdk.TestData key;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            logger.info("Initiating execution");
            AndroidDriver androidDriver = (AndroidDriver)this.driver;
            AndroidKey androidKey = KeyUtil.getKey(key.getValue().toString());
            androidDriver.longPressKey(new KeyEvent(androidKey));
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