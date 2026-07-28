package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

@Action(actionText = "Add data data-to-copy to clipboard",
        description = "This action copies the specified data to the system clipboard. " +
                "The data can then be pasted using Ctrl+V or other paste operations. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Add data to clipboard",
        useCustomScreenshot = true)
public class AddDataToClipboard extends WindowsAdvancedAction {

    @TestData(reference = "data-to-copy")
    private com.testsigma.sdk.TestData dataToCopy;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        logger.info("=== Add Data To Clipboard: Starting Execution ===");
        
        try {
            String data = dataToCopy.getValue().toString();
            
            logger.info("Adding data to clipboard: " + data);
            
            // Validate data
            if (data == null) {
                setErrorMessage("Data to copy cannot be null");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "add_data_to_clipboard_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Get the system clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            
            // Create a StringSelection object with the data
            StringSelection stringSelection = new StringSelection(data);
            
            // Set the clipboard contents
            clipboard.setContents(stringSelection, null);
            
            // Verify the data was copied successfully
            Transferable clipboardContents = clipboard.getContents(null);
            if (clipboardContents != null && clipboardContents.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
                try {
                    String clipboardData = (String) clipboardContents.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    if (data.equals(clipboardData)) {
                        String successMessage = String.format(
                            "Successfully copied data to clipboard: <b>%s</b>",
                            data
                        );
                        setSuccessMessage(successMessage);
                        logger.info("Successfully copied data to clipboard: " + data);
                        
                        // Capture and upload screenshot
                        ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                                "add_data_to_clipboard_screenshot", logger);
                        
                        return Result.SUCCESS;
                    } else {
                        setErrorMessage("Data verification failed - clipboard contents do not match expected data");
                        ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                                "add_data_to_clipboard_failure_screenshot", logger);
                        return Result.FAILED;
                    }
                } catch (Exception e) {
                    setErrorMessage("Error verifying clipboard contents: " + e.getMessage());
                    ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                            "add_data_to_clipboard_failure_screenshot", logger);
                    return Result.FAILED;
                }
            } else {
                setErrorMessage("Failed to copy data to clipboard - clipboard operation not supported");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "add_data_to_clipboard_failure_screenshot", logger);
                return Result.FAILED;
            }
            
        } catch (Exception e) {
            String errorMessage = "Error copying data to clipboard: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("Exception during clipboard operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "add_data_to_clipboard_failure_screenshot", logger);
            return Result.FAILED;
        }
    }
}
