package com.testsigma.addons.windowsLite;

import com.testsigma.addons.util.KeyboardUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;

@Data
@Action(actionText = "Press two modifier keys together with a specific key: Modifier Key 1: key-type-1, Modifier Key 2: key-type-2, Key: test-data",
        description = "This action allows you to press two modifier keys together with a specific key on the keyboard. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS,
        displayName = "Press two Modifier Keys with a basic Key",
        useCustomScreenshot = true)
public class PressTwoModifierKeysWithBasicKey extends WindowsAction {

    @TestData(reference = "key-type-1",
            allowedValues = {"Alt", "BackSpace", "CapsLock", "Ctrl", "Delete", "Down",
                    "Enter", "Esc", "Left", "Right", "Shift", "Tab", "Up", "WINDOW"})
    private com.testsigma.sdk.TestData keyType1;

    @TestData(reference = "key-type-2",
            allowedValues = {"Alt", "BackSpace", "CapsLock", "Ctrl", "Delete", "Down",
                    "Enter", "Esc", "Left", "Right", "Shift", "Tab", "Up", "WINDOW"})
    private com.testsigma.sdk.TestData keyType2;

    @TestData(reference = "test-data", allowedValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "Space", "Comma", "Period", "Semicolon", "Colon", "Exclamation", "Question",
            "At", "Hash", "Dollar", "Percent", "Caret", "Ampersand", "Asterisk", "Left_Parenthesis", "Right_Parenthesis",
            "Minus", "Plus", "Equals", "Left_Bracket", "Right_Bracket", "Backslash", "Forward_Slash", "Pipe",
            "Left_Brace", "Right_Brace", "Tilde", "Backtick", "Quote", "Double_Quote"})
    private com.testsigma.sdk.TestData testData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String modifierKey1 = keyType1.getValue().toString();
            String modifierKey2 = keyType2.getValue().toString();
            String specificKey = testData.getValue().toString();
            
            logger.info("Modifier Key 1: " + modifierKey1);
            logger.info("Modifier Key 2: " + modifierKey2);
            logger.info("Specific Key: " + specificKey);

            // Convert the modifier keys to their corresponding KeyEvent constants
            int modifierKeyCode1 = KeyboardUtils.getModifierKeyCode(modifierKey1);
            int modifierKeyCode2 = KeyboardUtils.getModifierKeyCode(modifierKey2);
            int specificKeyCode = KeyboardUtils.getSpecificKeyCode(specificKey);

            logger.info("Modifier Key Code 1: " + modifierKeyCode1);
            logger.info("Modifier Key Code 2: " + modifierKeyCode2);
            logger.info("Specific Key Code: " + specificKeyCode);

            // Press both modifier keys first, then the specific key
            robot.keyPress(modifierKeyCode1);
            KeyboardUtils.sleep(30);
            robot.keyPress(modifierKeyCode2);
            KeyboardUtils.sleep(30);
            robot.keyPress(specificKeyCode);
            KeyboardUtils.sleep(30);

            // Release keys in reverse order
            robot.keyRelease(specificKeyCode);
            KeyboardUtils.sleep(30);
            robot.keyRelease(modifierKeyCode2);
            KeyboardUtils.sleep(30);
            robot.keyRelease(modifierKeyCode1);
            
            
            setSuccessMessage("Successfully pressed " + modifierKey1 + " + " + modifierKey2 + " + " + specificKey + " keys.");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_two_modifier_keys_with_basic_key_screenshot", logger);
            
        } catch (Exception e) {
            setErrorMessage("An error occurred while pressing the two modifier keys with specific key: " + e.getMessage());
            logger.debug("Error pressing two modifier keys with specific key: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "press_two_modifier_keys_with_basic_key_failure_screenshot", logger);
        }
        return result;
    }
    

} 