package com.testsigma.addons.web;

import com.testsigma.addons.util.DownloadUtilities;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(actionText = "Verify if the latest downloaded file type is expected-file-type",
        description = "Verifies that the most recently downloaded file has the expected file type/extension",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyIfTheLatestFileType extends WebAction {

    @TestData(reference = "expected-file-type")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;

        String currentWindowHandle = driver.getWindowHandle();
        try {
            String expectedFileType = testData.getValue().toString().trim().toLowerCase().replaceAll("^\\.", "");
            logger.info("Expected file type: " + expectedFileType);

            DownloadUtilities downloadUtilities = new DownloadUtilities(driver, logger);
            String downloadedFilePath = downloadUtilities.getDownloadedFileName();
            logger.info("Downloaded file path: " + downloadedFilePath);

            String fileName = new File(downloadedFilePath).getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
                setErrorMessage("Could not determine file type of downloaded file: " + fileName);
                return Result.FAILED;
            }
            String actualFileType = fileName.substring(dotIndex + 1).toLowerCase();
            logger.info("Actual file type: " + actualFileType);

            if (actualFileType.equals(expectedFileType)) {
                setSuccessMessage("Verified: the latest downloaded file type is '" + actualFileType + "' as expected.");
            } else {
                setErrorMessage("File type mismatch: expected '" + expectedFileType + "' but found '" + actualFileType + "'.");
                result = Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Error while verifying file type: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error while verifying file type: " + e.getMessage());
            result = Result.FAILED;
        } finally {
            driver.switchTo().window(currentWindowHandle);
        }

        return result;
    }
}
