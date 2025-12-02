package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;

@Data
@Action(
        actionText = "Delete all contents inside the folder at locationFile",
        description = "Deletes all files and sub-folders inside the given folder but keeps the folder itself",
        applicationType = ApplicationType.WEB
)
public class DeleteFolderContents extends WebAction {

    @TestData(reference = "locationFile")
    private com.testsigma.sdk.TestData locationFile;

    @Override
    public Result execute() {
        logger.info("Starting delete operation...");
        Result result = Result.SUCCESS;

        try {
            File folder = new File(locationFile.getValue().toString());

            if (!folder.exists()) {
                logger.warn("Folder does not exist: " + folder.getAbsolutePath());
                setErrorMessage("Folder does not exist: " + folder.getAbsolutePath());
                return Result.FAILED;
            }

            if (!folder.isDirectory()) {
                logger.warn("Provided path is not a folder: " + folder.getAbsolutePath());
                setErrorMessage("Provided path is not a folder: " + folder.getAbsolutePath());
                return Result.FAILED;
            }

            boolean deleted = deleteContentsOnly(folder);

            if (deleted) {
                logger.info("Deleted all contents inside: " + folder.getAbsolutePath());
                setSuccessMessage("Deleted all contents inside: " + folder.getAbsolutePath());
            } else {
                logger.warn("Failed to delete contents inside: " + folder.getAbsolutePath());
                setErrorMessage("Failed to delete contents inside: " + folder.getAbsolutePath());
                result = Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("Exception occurred while deleting file or folder: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while deleting file or folder: " + ExceptionUtils.getMessage(e));
            result  = Result.FAILED;
        }

        return result;
    }

    // Delete everything inside folder except the folder itself
    private boolean deleteContentsOnly(File folder) {
        File[] allContents = folder.listFiles();

        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) {
                    // delete entire subfolder
                    deleteDirectory(file);
                } else {
                    // delete file
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Recursive delete directory + its contents
    private boolean deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    if (!f.delete()) return false;
                }
            }
        }
        return dir.delete();
    }
}
