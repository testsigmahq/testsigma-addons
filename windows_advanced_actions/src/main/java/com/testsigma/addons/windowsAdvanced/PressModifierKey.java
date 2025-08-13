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
@Action(actionText = "Press a modifier key key-type",
        description = "This action allows you to press a modifier key on the keyboard.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressModifierKey")
public class PressModifierKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab", "WINDOW"})
    private com.testsigma.sdk.TestData keyType;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String key = keyType.getValue().toString();

            // Convert the modifier key to its corresponding KeyEvent constant
            int keyCode = KeyboardUtils.getModifierKeyCode(key);

            // Press and release the modifier key
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);

            setSuccessMessage("Successfully pressed the " + key + " key.");
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the modifier key: " + e.getMessage());
            logger.debug("Error pressing modifier key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
} 