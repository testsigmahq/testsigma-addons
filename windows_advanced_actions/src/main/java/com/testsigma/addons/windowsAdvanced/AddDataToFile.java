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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Action(actionText = "Add data file-content to the file file-path",
        description = "This action adds the specified data to a file at the given path. " +
                "If the file doesn't exist, it will be created. " +
                "The data will be appended to the end of the file. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Add data to file",
        useCustomScreenshot = true)
public class AddDataToFile extends WindowsAdvancedAction {

    @TestData(reference = "file-content")
    private com.testsigma.sdk.TestData fileContents;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        logger.info("=== Add Data To File: Starting Execution ===");
        
        try {
            String content = fileContents.getValue().toString();
            String path = filePath.getValue().toString();
            
            logger.info("Adding data to file: " + path);
            logger.info("Data to add: " + content);
            
            // Validate file path
            if (path == null || path.trim().isEmpty()) {
                setErrorMessage("File path cannot be empty");
                ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                        "add_data_to_file_failure_screenshot", logger);
                return Result.FAILED;
            }
            
            // Create Path object and ensure parent directories exist
            Path filePathObj = Paths.get(path);
            Path parentDir = filePathObj.getParent();
            
            if (parentDir != null && !Files.exists(parentDir)) {
                logger.info("Creating parent directories: " + parentDir);
                Files.createDirectories(parentDir);
            }
            
            // Create or append to file
            File file = filePathObj.toFile();
            boolean fileExisted = file.exists();
            
            try (FileWriter writer = new FileWriter(file, true)) { // true for append mode
                writer.write(content);
                writer.flush();
            }
            
            String action = fileExisted ? "appended to" : "created and written to";
            String successMessage = String.format(
                "Successfully %s file: <b>%s</b> with data: <b>%s</b>",
                action, path, content
            );
            
            setSuccessMessage(successMessage);
            logger.info("Successfully " + action + " file: " + path);
            
            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "add_data_to_file_screenshot", logger);
            
            return Result.SUCCESS;
            
        } catch (IOException e) {
            String errorMessage = "Error writing to file: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("IOException during file write operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "add_data_to_file_failure_screenshot", logger);
            return Result.FAILED;
            
        } catch (Exception e) {
            String errorMessage = "Unexpected error during file operation: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.debug("Exception during file operation: " + ExceptionUtils.getStackTrace(e));
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, 
                    "add_data_to_file_failure_screenshot", logger);
            return Result.FAILED;
        }
    }
}
