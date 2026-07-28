package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Action(actionText = "Erase data in file file-path",
        description = "This action erases all data from the specified file. " +
                "The file will be truncated to zero length, effectively removing all content. " +
                "If the file doesn't exist, an error will be returned. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Erase data in file",
        useCustomScreenshot = true)
public class EraseDataInFile extends WindowsAdvancedAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        logger.info("=== Erase Data In File: Starting Execution ===");
        
        try {
            String path = filePath.getValue().toString();
            
            logger.info("Erasing data from file: " + path);
            
            // Validate file path
            if (path == null || path.trim().isEmpty()) {
                setErrorMessage("File path cannot be empty");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "erase_data_in_file_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Check if file exists
            Path filePathObj = Paths.get(path);
            File file = filePathObj.toFile();
            
            if (!file.exists()) {
                String errorMessage = "File does not exist: " + path;
                setErrorMessage(errorMessage);
                logger.info(errorMessage);
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "erase_data_in_file_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Check if file is writable
            if (!file.canWrite()) {
                String errorMessage = "File is not writable: " + path;
                setErrorMessage(errorMessage);
                logger.info(errorMessage);
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "erase_data_in_file_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Get file size before erasing for logging
            long fileSizeBefore = file.length();
            logger.info("File size before erasing: " + fileSizeBefore + " bytes");
            
            // Erase file content by truncating it
            try (FileWriter writer = new FileWriter(file, false)) { // false for overwrite mode
                // Write nothing, effectively truncating the file
                writer.write("");
                writer.flush();
            }
            
            // Verify file is now empty
            long fileSizeAfter = file.length();
            logger.info("File size after erasing: " + fileSizeAfter + " bytes");
            
            String successMessage = String.format(
                "Successfully erased all data from file: <b>%s</b>. " +
                "File size changed from <b>%d</b> bytes to <b>%d</b> bytes",
                path, fileSizeBefore, fileSizeAfter
            );
            
            setSuccessMessage(successMessage);
            logger.info("Successfully erased data from file: " + path);
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "erase_data_in_file_screenshot", logger);
            
            return Result.SUCCESS;
            
        } catch (IOException e) {
            String errorMessage = "Error erasing file content: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("IOException during file erase operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "erase_data_in_file_failure_screenshot", logger);
            return Result.FAILED;
            
        } catch (Exception e) {
            String errorMessage = "Unexpected error during file erase operation: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("Exception during file erase operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "erase_data_in_file_failure_screenshot", logger);
            return Result.FAILED;
        }
    }
}
