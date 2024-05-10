package com.testsigma.addons.web;

import com.testsigma.addons.web.folderutil.FolderUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;


import java.io.File;

@Data
@Action(actionText = "Verify that the folder Folder-Path has a file with name containing File-Name",
        description = "Verifies whether the folder has any file containing the given name",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyFileContainingNamePresence extends WebAction {


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
        File file = util.fileContainsName(folderPath, fileName);
        if (file != null) {
            setSuccessMessage(util.successInfo);
        } else {
            result = Result.FAILED;
            setErrorMessage(util.errorInfo);
        }
        logger.info("Execution completed");
        return result;
    }
}
