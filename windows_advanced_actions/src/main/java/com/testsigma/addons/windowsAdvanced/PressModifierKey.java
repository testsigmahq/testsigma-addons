package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.addons.util.KeyboardUtils;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;

@Data
@Action(actionText = "Press a modifier key key-type",
        description = "This action allows you to press a modifier key on the keyboard. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Press a Modifier Key",
        useCustomScreenshot = true)
public class PressModifierKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type", allowedValues = {"Alt", "BackSpace", "CapsLock", "Ctrl", "Delete", "Down",
            "Enter", "Esc", "Left", "Right", "Shift", "Tab", "Up", "WINDOW"})
    private com.testsigma.sdk.TestData keyType;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String key = keyType.getValue().toString();

            // Convert the modifier key to its corresponding KeyEvent constant
            int keyCode = KeyboardUtils.getModifierKeyCode(key);
            logger.info("key code " + keyCode);

            // Press and release the modifier key
            robot.keyPress(keyCode);
            // Adding a small delay to ensure the key press is registered
            KeyboardUtils.sleep(30);
            robot.keyRelease(keyCode);
            setSuccessMessage("Successfully pressed the " + key + " key.");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_modifier_key_screenshot", logger);
            
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the modifier key: " + e.getMessage());
            logger.debug("Error pressing modifier key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_modifier_key_failure_screenshot", logger);
        }
        return result;
    }
    

} 