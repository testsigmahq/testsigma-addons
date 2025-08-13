package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.windowsAdvanced.utils.KeyboardUtils;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;

@Data
@Action(actionText = "Press two modifier keys together with a specific key: Modifier Key 1: key-type-1, Modifier Key 2: key-type-2, Key: test-data",
        description = "This action allows you to press two modifier keys together with a specific key on the keyboard.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressTwoModifierKeysWithSpecificKey")
public class PressTwoModifierKeysWithSpecificKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type-1", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab", "WINDOW"})
    private com.testsigma.sdk.TestData keyType1;

    @TestData(reference = "key-type-2", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab"})
    private com.testsigma.sdk.TestData keyType2;

    @TestData(reference = "test-data", allowedValues = {"Space", "Backspace", "Delete", "Escape", "Home", "End", 
                                                        "Page_Up", "Page_Down", "Insert", "F1", "F2", "F3", "F4", 
                                                        "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"})
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String modifierKey1 = keyType1.getValue().toString();
            String modifierKey2 = keyType2.getValue().toString();
            String specificKey = testData.getValue().toString();

            // Convert the modifier keys to their corresponding KeyEvent constants
            int modifierKeyCode1 = KeyboardUtils.getModifierKeyCode(modifierKey1);
            int modifierKeyCode2 = KeyboardUtils.getModifierKeyCode(modifierKey2);
            int specificKeyCode = KeyboardUtils.getSpecificKeyCode(specificKey);

            // Press both modifier keys first, then the specific key
            robot.keyPress(modifierKeyCode1);
            robot.keyPress(modifierKeyCode2);
            robot.keyPress(specificKeyCode);
            
            // Small delay
            Thread.sleep(100);
            
            // Release keys in reverse order
            robot.keyRelease(specificKeyCode);
            robot.keyRelease(modifierKeyCode2);
            robot.keyRelease(modifierKeyCode1);

            setSuccessMessage("Successfully pressed " + modifierKey1 + " + " + modifierKey2 + " + " + specificKey + " keys.");
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the two modifier keys with specific key: " + e.getMessage());
            logger.debug("Error pressing two modifier keys with specific key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
} 