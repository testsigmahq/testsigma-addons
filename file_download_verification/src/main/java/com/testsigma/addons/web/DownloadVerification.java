package com.testsigma.addons.web;

import com.testsigma.addons.util.DownloadUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if file with name file-name is successfully downloaded",
        description = "Verifies if the file with given name is downloaded. Eg.: File1",
        applicationType = ApplicationType.WEB)
public class DownloadVerification extends WebAction {

    @TestData(reference = "file-name")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result;
        String currentWindowHandle = driver.getWindowHandle();
        try {
            DownloadUtilities downloadUtilities = new DownloadUtilities(driver, logger);
            logger.info("Searching for file");
            String fileNameToBeSearched = testData.getValue().toString();
            boolean isFileDownloaded = downloadUtilities.SearchForTheFile(fileNameToBeSearched);
            if (isFileDownloaded) {
                setSuccessMessage("File with name '" + testData.getValue() + "' was successfully downloaded.");
                result = Result.SUCCESS;
            } else {
                setErrorMessage("File with name '" + testData.getValue() + "' was not found in the downloads.");
                result = Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("File with name '" + testData.getValue() + "' was not found in the downloads.");
            result = Result.FAILED;
        } finally {
            logger.info("Switching back to the current window");
            driver.switchTo().window(currentWindowHandle);
        }
        return result;
    }
}
