package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.KeyEvent;


@Data
@Action(actionText = "Press Function Key key-type",
        description = "This action allows you to press a function key on the keyboard.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressFunctionKey")
public class PressFunctionKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type",allowedValues = {"F1", "F2", "F3", "F4", "F5", "F6",
            "F7", "F8", "F9", "F10", "F11", "F12"})
    private com.testsigma.sdk.TestData keyType;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new java.awt.Robot();
            String key = keyType.getValue().toString();

            // Convert the function key to its corresponding KeyEvent constant
            int keyCode = KeyEvent.class.getField("VK_" + key).getInt(null);

            // Press and release the function key
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);

            setSuccessMessage("Successfully pressed the " + key + " key.");
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the function key: " + e.getMessage());
            logger.debug("Error pressing function key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;        }
        return result;
    }
}
