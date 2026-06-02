package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.windowsAdvanced.utils.ScreenshotUtils;
import com.testsigma.addons.windowsAdvanced.utils.KeyboardUtils;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.KeyEvent;


@Data
@Action(actionText = "Press Function Key key-type",
        description = "This action allows you to press a function key on the keyboard. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressFunctionKey", 
        useCustomScreenshot = true
        )
public class PressFunctionKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type",allowedValues = {"F1", "F2", "F3", "F4", "F5", "F6",
            "F7", "F8", "F9", "F10", "F11", "F12"})
    private com.testsigma.sdk.TestData keyType;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new java.awt.Robot();
            String key = keyType.getValue().toString();

            // Convert the function key to its corresponding KeyEvent constant
            int keyCode = KeyEvent.class.getField("VK_" + key).getInt(null);
            logger.info("Key Code: " + keyCode);

            // Press and release the function key
            robot.keyPress(keyCode);
            KeyboardUtils.sleep(30);
            robot.keyRelease(keyCode);
            logger.info("Key Released: " + keyCode);

            setSuccessMessage("Successfully pressed the " + key + " key.");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_function_key_screenshot", logger);
            
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the function key: " + e.getMessage());
            logger.debug("Error pressing function key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_function_key_failure_screenshot", logger);
        }
        return result;
    }
    

}
