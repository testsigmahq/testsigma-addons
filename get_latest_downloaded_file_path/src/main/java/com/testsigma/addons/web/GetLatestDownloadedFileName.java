package com.testsigma.addons.web;

import com.testsigma.addons.web.util.Utilities;
import com.testsigma.addons.web.util.UtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Action(actionText = "Get latest downloaded content and store in runtime variable variable-name",
        description = "Retrieves the local path of the most recently downloaded file from the browser" +
                " (Chrome/Edge) and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB)
public class GetLatestDownloadedFileName extends WebAction {

    @TestData(reference = "content", allowedValues = {"file-name", "file-path", "file-name-with-extension", "file-extension"})
    private com.testsigma.sdk.TestData content;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        Utilities utilities = UtilitiesFactory.create(driver, logger);
        String originalWindowHandle = driver.getWindowHandle();
        try {
            logger.info("Initiated execution");
            String contentType = content.getValue().toString().trim().toLowerCase();

            String storedValue;
            if ("file-path".equals(contentType)) {
                // Copy the file locally and return the temp file path
                File localFile = utilities.copyFileFromDownloads();
                logger.info("Local file path: " + localFile.getAbsolutePath());
                storedValue = localFile.getAbsolutePath();
            } else {
                // For name/extension, read the original file name from the downloads page
                // without copying the entire file
                ((JavascriptExecutor) driver).executeScript("window.open('about:blank','_blank');");
                List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(tabs.size() - 1));
                try {
                    if (!utilities.isFileDownloaded()) {
                        throw new RuntimeException("File is still downloading.");
                    }
                    String originalPath = utilities.getDownloadedFileLocalPath();
                    logger.info("Original downloaded file path: " + originalPath);

                    int lastSep = Math.max(originalPath.lastIndexOf('/'), originalPath.lastIndexOf('\\'));
                    String originalFileName = lastSep >= 0 ? originalPath.substring(lastSep + 1) : originalPath;
                    int dotIndex = originalFileName.lastIndexOf('.');

                    switch (contentType) {
                        case "file-name":
                            storedValue = dotIndex > 0 ? originalFileName.substring(0, dotIndex) : originalFileName;
                            break;
                        case "file-name-with-extension":
                            storedValue = originalFileName;
                            break;
                        case "file-extension":
                            storedValue = dotIndex > 0 ? originalFileName.substring(dotIndex + 1) : "";
                            break;
                        default:
                            storedValue = originalFileName;
                            break;
                    }
                } finally {
                    driver.close();
                    driver.switchTo().window(originalWindowHandle);
                }
            }

            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(storedValue);
            setSuccessMessage("Successfully stored the latest downloaded " + contentType + " in the runtime variable. "
                    + runtimeVariable.getValue().toString() + " = " + storedValue);
        } catch (Exception e) {
            logger.info("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception Occurred while extracting the latest downloaded file content. Exception: "
                    + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }
        return result;
    }

}
