package com.testsigma.addons.web;

import com.testsigma.addons.util.DownloadUtilities;
import com.testsigma.addons.util.DownloadUtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Data
@Action(actionText = "Verify if file with name file-name (with extension) is successfully downloaded",
        description = "Verifies if the file with given name is downloaded. Eg.: File1.pdf",
        applicationType = ApplicationType.WEB)
public class DownloadVerification extends WebAction {

    @TestData(reference = "file-name")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result;
        String currentWindowHandle = driver.getWindowHandle();
        try {
            DownloadUtilities downloadUtilities = DownloadUtilitiesFactory.create(driver, logger);
            logger.info("Searching for file");
            String fileNameToBeSearched = testData.getValue().toString();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            wait.pollingEvery(Duration.ofMillis(500));
            boolean isFileDownloaded = wait.until(d -> checkFileDownloadStatus(downloadUtilities, fileNameToBeSearched));
            if (isFileDownloaded) {
                logger.info("File with name " + fileNameToBeSearched + " is found");
                setSuccessMessage("File with name '" + testData.getValue() + "' was successfully downloaded.");
                result = Result.SUCCESS;
            } else {
                logger.info("File with name " + fileNameToBeSearched + " is not found");
                setErrorMessage("File with name '" + testData.getValue() + "' was not found in the downloads.");
                result = Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("File with name '" + testData.getValue() + "' was not found in the downloads. Exception: " + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        } finally {
            logger.info("Switching back to the current window");
            driver.switchTo().window(currentWindowHandle);
        }
        return result;
    }

    private boolean checkFileDownloadStatus(DownloadUtilities downloadUtilities, String fileNameToBeSearched) {
        return downloadUtilities.searchFileNameInDownloads(fileNameToBeSearched);
    }
}
