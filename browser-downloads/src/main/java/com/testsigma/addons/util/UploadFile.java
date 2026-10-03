package com.testsigma.addons.util;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "File Upload: Upload file File-Path to the element element-name",
        description = "This addon uploads a file to an <input type='file'> element",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class UploadFile extends WebAction {

    @TestData(reference = "File-Path")
    private com.testsigma.sdk.TestData filePath;

    @Element(reference = "element-name")
    private com.testsigma.sdk.Element element;

    @Override
    protected Result execute() throws NoSuchElementException {

        Result result = Result.SUCCESS;

        try {
            String tempFilePath = filePath.getValue().toString();
            logger.info("Received File Path: " + tempFilePath);
            
            File file;
            if (tempFilePath.startsWith("http://") || tempFilePath.startsWith("https://")) {
                logger.info("Downloading file from URL: " + tempFilePath);
                file = downloadFile(tempFilePath);
            } else {
                file = new File(tempFilePath);
            }

            if (!file.exists()) {
                File resolvedFile = resolveLocalFile(tempFilePath);
                if (resolvedFile != null && resolvedFile.exists()) {
                    logger.info("Resolved file path to: " + resolvedFile.getAbsolutePath());
                    file = resolvedFile;
                } else {
                    setErrorMessage("File not found in execution environment: " + tempFilePath);
                    logger.warn("File does not exist: " + tempFilePath);
                    return Result.FAILED;
                }
            }

            String absolutePath = file.getAbsolutePath();
            logger.info("Using absolute path for upload: " + absolutePath);

            // Upload file using Selenium sendKeys
            WebElement element1 = element.getElement();
            element1.sendKeys(absolutePath);

            setSuccessMessage("Successfully uploaded the file to the element");

        } catch (Exception e) {
            logger.warn("Upload failed: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to upload the file: " + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }

        return result;
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String originalName = Paths.get(url.getPath()).getFileName().toString();

        File tempFile = File.createTempFile("xml_download_", "_" + originalName);

        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        return tempFile;
    }

    private File resolveLocalFile(String tempFilePath) {
        String trimmedPath = tempFilePath == null ? "" : tempFilePath.trim();
        if (trimmedPath.isEmpty()) {
            return null;
        }

        String fileName = new File(trimmedPath).getName();
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.trim().isEmpty()) {
            File downloadsPath = new File(new File(userHome, "Downloads"), fileName);
            if (downloadsPath.exists()) {
                return downloadsPath;
            }
        }

        File currentDirPath = new File(System.getProperty("user.dir"), fileName);
        if (currentDirPath.exists()) {
            return currentDirPath;
        }

        return null;
    }
}
