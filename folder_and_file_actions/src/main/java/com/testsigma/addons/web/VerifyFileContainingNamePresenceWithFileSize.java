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
@Action(actionText = "Verify that the folder Folder-Path has a file with name containing File-Name and the size of " +
        "the file is greater than test-data KB",
        description = "Verifies whether the folder has any file containing the given name and verifies it's size " +
                "with given input size",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyFileContainingNamePresenceWithFileSize extends WebAction {


    @TestData(reference = "Folder-Path")
    private com.testsigma.sdk.TestData folderPath_;

    @TestData(reference = "File-Name")
    private com.testsigma.sdk.TestData fileName_;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData size_;


    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution...");
        Result result = Result.SUCCESS;
        String folderPath = folderPath_.getValue().toString();
        String fileName = fileName_.getValue().toString();
        long size = Long.parseLong(size_.getValue().toString());

        logger.info("Given folder path : " + folderPath);
        logger.info("Given file name : " + fileName);
        logger.info("Given size value : " + size);

        FolderUtilities util = new FolderUtilities();

        if (util.folderCheck(folderPath)) {
            File file = util.searchFile(new File(folderPath), fileName, false);
            if (file != null) {
                long fileSize = file.length() / 1000;
                if (file.length() > (size * 1000)) {
                    setSuccessMessage(String.format("Successfully verified that file exists with containing name %s in" +
                                    " the folder path %s and it's size %s KB is greater than %s KB", fileName, folderPath,
                            fileSize, size));
                } else {
                    result = Result.FAILED;
                    setErrorMessage(String.format("The files exists with containing name %s in the folder path %s and its" +
                            " size %s KB is not greater than %s KB", fileName, folderPath, fileSize, size));
                }
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
