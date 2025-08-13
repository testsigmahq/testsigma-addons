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
@Action(actionText = "Press a modifier key key-type with an alphanumeric key alphanumeric-key",
        description = "This action allows you to press a modifier key with an alphanumeric key on the keyboard.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "PressModifierKeyWithAlphanumericKey")
public class PressModifierKeyWithAlphanumericKey extends WindowsAdvancedAction {

    @TestData(reference = "key-type", allowedValues = {"Alt", "Ctrl", "Enter", "Shift", "Tab", "WINDOW"})
    private com.testsigma.sdk.TestData keyType;

    @TestData(reference = "alphanumeric-key", allowedValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", 
                                                               "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
                                                               "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
                                                               "U", "V", "W", "X", "Y", "Z",
                                                               "Space", "Comma", "Period", "Semicolon", "Colon", "Exclamation", "Question",
                                                               "At", "Hash", "Dollar", "Percent", "Caret", "Ampersand", "Asterisk",
                                                               "Left_Parenthesis", "Right_Parenthesis", "Minus", "Plus", "Equals",
                                                               "Left_Bracket", "Right_Bracket", "Backslash", "Forward_Slash", "Pipe",
                                                               "Left_Brace", "Right_Brace", "Tilde", "Backtick", "Quote", "Double_Quote"})
    private com.testsigma.sdk.TestData alphanumericKey;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String modifierKey = keyType.getValue().toString();
            String alphanumericKeyValue = alphanumericKey.getValue().toString();

            // Convert the modifier key to its corresponding KeyEvent constant
            int modifierKeyCode = KeyboardUtils.getModifierKeyCode(modifierKey);
            int alphanumericKeyCode = KeyboardUtils.getAlphanumericKeyCode(alphanumericKeyValue);

            // Press modifier key, then alphanumeric key
            robot.keyPress(modifierKeyCode);
            robot.keyPress(alphanumericKeyCode);
            
            // Small delay
            Thread.sleep(100);
            
            // Release keys in reverse order
            robot.keyRelease(alphanumericKeyCode);
            robot.keyRelease(modifierKeyCode);

            setSuccessMessage("Successfully pressed " + modifierKey + " + " + alphanumericKeyValue + " keys.");
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the modifier key with alphanumeric key: " + e.getMessage());
            logger.debug("Error pressing modifier key with alphanumeric key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
} 