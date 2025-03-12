package com.testsigma.addons;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Action(actionText = "Copy the latest downloaded CSV file and save the file path in runtime-variable variable-name",
        description = "This addon will Copy the latest downloaded file and save the file path in runtime-variable variable-name",
        applicationType = ApplicationType.WEB)
public class CaptureDownloadedCSVFilePath extends WebAction {

    @TestData(reference = "variable-name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        FileUtilities pdfAndDocUtilities = new FileUtilities(driver,logger);

        try {
            logger.info("Initiated execution");
            File downloadedExcelFile = pdfAndDocUtilities.copyFileFromDownloads("csv",null);

            logger.info("Local path"+downloadedExcelFile.getAbsolutePath());
            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(downloadedExcelFile.getAbsolutePath());
            logger.info("File Path saved in agent machine:"+downloadedExcelFile.getAbsolutePath());
            setSuccessMessage("Successfully copied the file from downloads and saved the copied location to variable:"+runTimeData.getKey());
        } catch (Exception e){
            logger.info("Unable to find the given file in the downloads"+ ExceptionUtils.getStackTrace(e));
            setErrorMessage(e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }
}

