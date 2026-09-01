package com.testsigma.addons.web;

import com.testsigma.addons.util.DownloadUtilities;
import com.testsigma.addons.util.FileUtilities;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(actionText = "Store the recent downloaded file Path to variable variable-name",
        description = "Store the recently downloaded file name to a variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class DownloadedFileName extends WebAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        FileUtilities pdfAndDocUtilities = new FileUtilities(driver, logger);

        try {
            logger.info("Initiated execution");
            File downloadedExcelFile = pdfAndDocUtilities.copyFileFromDownloads("xlsx", null);

            logger.info("Local path" + downloadedExcelFile.getAbsolutePath());
            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(downloadedExcelFile.getAbsolutePath());
            logger.info("File Path saved in agent machine:" + downloadedExcelFile.getAbsolutePath());
            setSuccessMessage("Successfully copied the file from downloads and saved the copied location to variable:" + runTimeData.getKey());
        } catch (Exception e) {
            logger.info("Unable to find the given file in the downloads" + ExceptionUtils.getStackTrace(e));
            setErrorMessage(e.getMessage());
            result = Result.FAILED;
        }
    /*
    try {
      String currentWindowHandle = driver.getWindowHandle();

      DownloadUtilities downloadUtilities = new DownloadUtilities(driver, logger);
      String fileName = downloadUtilities.getDownloadedFileName();
      runTimeData = new com.testsigma.sdk.RunTimeData();
      runTimeData.setKey(testData.getValue().toString());
      runTimeData.setValue(fileName);
    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info(ExceptionUtils.getStackTrace(error));
      setErrorMessage("Error while getting the downloaded file name::"+error.getMessage());
        return result;
    }finally {
      driver.switchTo().window(currentWindowHandle);
    }*/
        setSuccessMessage("Successfully stored the downloaded file name in variable::" + testData.getValue().toString() + " with value::" + runTimeData.getValue());
        return result;
    }
}