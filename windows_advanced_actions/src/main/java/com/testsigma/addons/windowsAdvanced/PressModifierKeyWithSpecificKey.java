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
@Action(actionText = "Press a modifier key key-type with a specific key test-data",
        description = "This action allows you to press a modifier key with a specific key on the keyboard.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressModifierKeyWithSpecificKey")
public class PressModifierKeyWithSpecificKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab", "WINDOW"})
    private com.testsigma.sdk.TestData keyType;

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
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the modifier key with specific key: " + e.getMessage());
            logger.debug("Error pressing modifier key with specific key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
} 