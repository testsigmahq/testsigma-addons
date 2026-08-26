package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;

@Data
@Action(actionText = "Delete all file from downloads",
        description = "Deletes all files from downloads",
        applicationType = ApplicationType.WEB)

public class DeleteAllFiles extends WebAction {

    @Override
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        try {

            String downloadsPath = System.getProperty("user.home") + "/Downloads";
            File downloadsFolder = new File(downloadsPath);
            logger.info("Downloads path resolved to: " + downloadsPath);

            if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
                File[] files = downloadsFolder.listFiles();
                if (files == null) {
                    // listFiles() returns null on an I/O error (e.g. permissions issue),
                    // even though the directory exists - guard against that here.
                    result = com.testsigma.sdk.Result.FAILED;
                    setErrorMessage("Could not list files in downloads folder (permission or I/O issue): " + downloadsPath);
                    return result;
                }
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }

               setSuccessMessage("All files are deleted from downloads");
            } else {
              result = com.testsigma.sdk.Result.FAILED;
              setErrorMessage("Downloads folder does not exist or is not a directory!");
            }

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        }
        return result;
    }
}
