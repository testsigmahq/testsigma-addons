package com.testsigma.addons.web;

import com.testsigma.addons.web.util.Utilities;
import com.testsigma.addons.web.util.UtilitiesFactory;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.nio.file.Files;

@Data
@Action(actionText = "Verify if the latest downloaded file type is expected-file-type",
        description = "Verifies that the most recently downloaded file has the expected file type/extension",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyIfTheLatestFileType extends WebAction {

    @TestData(reference = "expected-file-type", allowedValues = {"pdf", "png", "jpg", "jpeg", "csv", "zip", "xlsx", "docx", "txt"})
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;

        String currentWindowHandle = driver.getWindowHandle();
        try {
            String expectedFileType = testData.getValue().toString().trim().toLowerCase().replaceAll("^\\.", "");
            logger.info("Expected file type: " + expectedFileType);

            String expectedMimeType;
            switch (expectedFileType) {
                case "pdf":  expectedMimeType = "application/pdf"; break;
                case "png":  expectedMimeType = "image/png"; break;
                case "jpg":
                case "jpeg": expectedMimeType = "image/jpeg"; break;
                case "csv":  expectedMimeType = "text/csv"; break;
                case "zip":  expectedMimeType = "application/zip"; break;
                case "xlsx": expectedMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; break;
                case "docx": expectedMimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; break;
                case "txt":  expectedMimeType = "text/plain"; break;
                default:
                    setErrorMessage("Unsupported file type: '" + expectedFileType + "'. Supported types: pdf, png, jpg, jpeg, csv, zip, xlsx, docx, txt");
                    return Result.FAILED;
            }
            logger.info("Expected MIME type: " + expectedMimeType);

            Utilities utilities = UtilitiesFactory.create(driver, logger);
            File localFile = utilities.copyFileFromDownloads();
            logger.info("Local temp file: " + localFile.getAbsolutePath());

            String actualFileType;
            try {
                actualFileType = Files.probeContentType(localFile.toPath());
            } finally {
                localFile.delete();
            }

            if (actualFileType == null) {
                setErrorMessage("Could not determine file type of downloaded file: " + localFile.getName());
                return Result.FAILED;
            }
            logger.info("Actual file type: " + actualFileType);

            if (actualFileType.equals(expectedMimeType)) {
                setSuccessMessage("Verified: the latest downloaded file type is '" + expectedFileType + "' as expected.");
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
