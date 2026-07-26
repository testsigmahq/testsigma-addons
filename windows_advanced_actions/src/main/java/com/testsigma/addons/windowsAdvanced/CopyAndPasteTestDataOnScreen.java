package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.addons.util.KeyboardUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

@Data
@Action(actionText = "Copy and paste given data on screen text-to-copy-paste",
        description = "This action allows you to copy and paste data on the screen using the keyboard. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Copy and paste given data on screen",
        useCustomScreenshot = true)
public class CopyAndPasteTestDataOnScreen extends WindowsAdvancedAction {
    
    @TestData(reference = "text-to-copy-paste")
    private com.testsigma.sdk.TestData testData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public com.testsigma.sdk.Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            StringSelection stringSelection = new StringSelection(testData.getValue().toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable data = clipboard.getContents(null);
            clipboard.setContents(stringSelection, null);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            KeyboardUtils.sleep(100);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            Thread.sleep(500);
            clipboard.setContents(data, null);
            setSuccessMessage("Given data copied and pasted successfully on the screen");
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "copy_paste_data_screenshot", logger);
            
        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("An error occurred while copying and pasting data: " + e.getMessage());
            logger.debug("Error copying and pasting data: " + ExceptionUtils.getStackTrace(e));
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "copy_paste_data_failure_screenshot", logger);
            return result;
        }
        return result;
    }
}
