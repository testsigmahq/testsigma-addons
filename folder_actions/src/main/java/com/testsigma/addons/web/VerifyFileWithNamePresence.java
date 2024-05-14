package com.testsigma.addons.web;

import com.testsigma.addons.web.folderutil.FolderUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(actionText = "Verify that the folder Folder-Path has a file with name File-Name",
        description = "Verifies whether the folder has a file with given name",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyFileWithNamePresence extends WebAction {


    @TestData(reference = "Folder-Path")
    private com.testsigma.sdk.TestData folderPath_;

    @TestData(reference = "File-Name")
    private com.testsigma.sdk.TestData fileName_;


    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution...");
        Result result = Result.SUCCESS;
        String folderPath = folderPath_.getValue().toString();
        String fileName = fileName_.getValue().toString();

        logger.info("Given folder path : " + folderPath);
        logger.info("Given file name : " + fileName);

        FolderUtilities util = new FolderUtilities();
        if (util.folderCheck(folderPath)) {
            File file = util.searchFile(new File(folderPath), fileName, true);
            if (file != null) {
                String message = String.format("Successfully verified that file exists with name %s in the folder " +
                        "path %s", fileName, folderPath);
                setSuccessMessage(message);
            } else {
                result = Result.FAILED;
                setErrorMessage(String.format(util.NO_FILE_EXISTS_ERROR_MSG, fileName, folderPath));
            }
        } else {
            result = Result.FAILED;
            setErrorMessage(String.format(util.FOLDER_NOT_FOUND, folderPath));
        }
        logger.info("Execution completed");
        return result;
    }
}