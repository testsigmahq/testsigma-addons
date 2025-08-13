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
@Action(actionText = "Press two modifier keys simultaneously: Modifier Key 1: key-type-1, Modifier Key 2: key-type-2",
        description = "This action allows you to press two modifier keys simultaneously on the keyboard.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressTwoModifierKeys")
public class PressTwoModifierKeys extends WindowsAdvancedAction {

    @TestData(reference = "key-type-1", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab", "WINDOW"})
    private com.testsigma.sdk.TestData keyType1;

    @TestData(reference = "key-type-2", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab"})
    private com.testsigma.sdk.TestData keyType2;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String key1 = keyType1.getValue().toString();
            String key2 = keyType2.getValue().toString();

            // Convert the modifier keys to their corresponding KeyEvent constants
            int keyCode1 = KeyboardUtils.getModifierKeyCode(key1);
            int keyCode2 = KeyboardUtils.getModifierKeyCode(key2);

            // Press both modifier keys simultaneously
            robot.keyPress(keyCode1);
            robot.keyPress(keyCode2);

            // Small delay to ensure both keys are pressed
            Thread.sleep(100);

            // Release both modifier keys
            robot.keyRelease(keyCode2);
            robot.keyRelease(keyCode1);

            setSuccessMessage("Successfully pressed the " + key1 + " and " + key2 + " keys simultaneously.");
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the two modifier keys: " + e.getMessage());
            logger.debug("Error pressing two modifier keys: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
} 