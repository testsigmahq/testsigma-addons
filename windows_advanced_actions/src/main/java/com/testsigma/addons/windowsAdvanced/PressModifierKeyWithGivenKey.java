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
@Action(actionText = "Press a modifier key key-type with a specific key test-data",
        description = "This action allows you to press a modifier key with a specific key on the keyboard. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Press a Modifier Key with a given Key",
        useCustomScreenshot = true)
public class PressModifierKeyWithGivenKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type", allowedValues = {"Alt", "BackSpace", "CapsLock", "Ctrl", "Delete", "Down",
            "Enter", "Esc", "Left", "Right", "Shift", "Tab", "Up", "WINDOW"})
    private com.testsigma.sdk.TestData keyType;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String modifierKey = keyType.getValue().toString();
            String specificKey = testData.getValue().toString();

            // Convert the modifier key to its corresponding KeyEvent constant
            int modifierKeyCode = KeyboardUtils.getModifierKeyCode(modifierKey);
            int specificKeyCode = KeyboardUtils.getSpecificKeyCode(specificKey);

            // Press modifier key, then specific key
            robot.keyPress(modifierKeyCode);
            robot.keyPress(specificKeyCode);
            
            // Small delay
            Thread.sleep(100);
            
            // Release keys in reverse order
            robot.keyRelease(specificKeyCode);
            robot.keyRelease(modifierKeyCode);

            setSuccessMessage("Successfully pressed " + modifierKey + " + " + specificKey + " keys.");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_modifier_key_with_given_key_screenshot", logger);
            
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the modifier key with specific key: " + e.getMessage());
            logger.debug("Error pressing modifier key with specific key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_modifier_key_with_given_key_failure_screenshot", logger);
        }
        return result;
    }
    

} 