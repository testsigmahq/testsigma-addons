package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.web.folderutil.FolderUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.io.File;
import java.util.NoSuchElementException;

@Data
@Action(actionText = "Verify that the folder Folder-Path has a file with name containing File-Name",
        description = "Verifies whether the folder has any file containing the given name",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Verify folder has file containing name",
        useCustomScreenshot = false)
public class VerifyFileContainingNamePresence extends WindowsAdvancedAction {

    @TestData(reference = "Folder-Path")
    private com.testsigma.sdk.TestData folderPath_;

    @TestData(reference = "File-Name")
    private com.testsigma.sdk.TestData fileName_;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution...");
        Result result = Result.SUCCESS;
        String folderPath = folderPath_.getValue().toString();
        String fileName = fileName_.getValue().toString();

        logger.info("Given folder path : " + folderPath);
        logger.info("Given file name : " + fileName);

        FolderUtilities util = new FolderUtilities();
        if (util.folderCheck(folderPath)) {
            File file = util.searchFile(new File(folderPath), fileName, false, logger);
            if (file != null) {
                setSuccessMessage(String.format("Successfully verified that file exists containing the name %s in " +
                        "the folder path %s", fileName, folderPath));
            } else {
                result = Result.FAILED;
                setErrorMessage(String.format(util.NO_FILE_CONTAINS_ERROR_MSG, fileName, folderPath));
            }
        } else {
            result = Result.FAILED;
            setErrorMessage(String.format(util.FOLDER_NOT_FOUND, folderPath));
        }
        logger.info("Execution completed");
        return result;
    }
}
