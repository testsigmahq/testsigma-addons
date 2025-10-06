package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Action(actionText = "Rename file existing-file-path with new name new-file-name",
        description = "Renames an existing file to a new name, handling file extensions automatically",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class RenameFileWebAction extends WebAction {

    @TestData(reference = "existing-file-path")
    private com.testsigma.sdk.TestData existingFilePath;
    
    @TestData(reference = "new-file-name")
    private com.testsigma.sdk.TestData newFileName;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        
        try {
            logger.info("Initiating file rename operation");
            logger.debug("Existing file path: " + existingFilePath.getValue() +
                    ", New file name: " + newFileName.getValue());
            
            String existingPath = existingFilePath.getValue().toString().trim();
            String newName = newFileName.getValue().toString().trim();
            
            // Validate inputs
            if (existingPath.isEmpty() || newName.isEmpty()) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Both existing file path and new file name must be provided and cannot be empty");
                return result;
            }
            
            // Convert to Path objects
            Path sourcePath = Paths.get(existingPath);
            File sourceFile = sourcePath.toFile();
            
            // Check if source file exists
            if (!sourceFile.exists()) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Source file does not exist: " + existingPath);
                return result;
            }
            
            // Check if source is a file (not directory)
            if (!sourceFile.isFile()) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Source path is not a file: " + existingPath);
                return result;
            }
            
            // Handle file extension logic
            String finalNewName = handleFileExtension(sourceFile.getName(), newName);
            
            // Create destination path in the same directory as source
            Path destinationPath = sourcePath.getParent().resolve(finalNewName);
            
            // Check if destination file already exists
            if (destinationPath.toFile().exists()) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Destination file already exists: " + destinationPath.toString());
                return result;
            }
            
            // Perform the rename operation
            Files.move(sourcePath, destinationPath);
            
            logger.info("File successfully renamed from " + existingPath + " to " + destinationPath.toString());
            setSuccessMessage("File successfully renamed from '" + sourceFile.getName() + "' to '" + finalNewName + "'");
            
        } catch (IOException e) {
            result = com.testsigma.sdk.Result.FAILED;
            String errorMsg = "Failed to rename file due to I/O error: " + e.getMessage();
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
        } catch (SecurityException e) {
            result = com.testsigma.sdk.Result.FAILED;
            String errorMsg = "Failed to rename file due to security/permission error: " + e.getMessage();
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            String errorMsg = "Unexpected error occurred while renaming file: " + e.getMessage();
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
        }
        
        return result;
    }
    
    /**
     * Handles file extension logic:
     * - If new name has extension, use it as is
     * - If new name doesn't have extension, preserve original extension
     */
    private String handleFileExtension(String originalFileName, String newFileName) {
        String originalExtension = getFileExtension(originalFileName);
        String newExtension = getFileExtension(newFileName);
        
        // If new name already has an extension, use it as is
        if (!newExtension.isEmpty()) {
            return newFileName;
        }
        
        // If new name doesn't have extension, preserve original extension
        if (!originalExtension.isEmpty()) {
            return newFileName + "." + originalExtension;
        }
        
        // If neither has extension, return new name as is
        return newFileName;
    }
    
    /**
     * Extracts file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        
        return "";
    }
}
