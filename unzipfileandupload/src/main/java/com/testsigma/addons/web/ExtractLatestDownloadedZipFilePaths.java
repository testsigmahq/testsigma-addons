package com.testsigma.addons.web;

import com.testsigma.addons.web.util.DownloadUtilities;
import com.testsigma.addons.web.util.DownloadUtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Data
@Action(
        actionText = "ZIP: Extract complete file paths from latest zip file in the downloads and store it in runtime-variable variable-name",
        description = "Extracts absolute file paths (excluding __MACOSX entries) from the latest downloaded zip file and stores them in a runtime variable",
        applicationType = ApplicationType.WEB
)
public class ExtractLatestDownloadedZipFilePaths extends WebAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;

        DownloadUtilities downloadUtilities =
                DownloadUtilitiesFactory.create(driver, logger);

        try {
            logger.info("Initiated execution");

            // Get latest downloaded ZIP file (copied to agent temp location)
            File downloadedZipFile =
                    downloadUtilities.copyFileFromDownloads("zip", null);

            logger.info("ZIP local path: " + downloadedZipFile.getAbsolutePath());

            StringBuilder filePaths = new StringBuilder();

            try (ZipFile zipFile = new ZipFile(downloadedZipFile)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                boolean first = true;

                // Base directory where ZIP exists
                File zipBaseDir = downloadedZipFile.getParentFile();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    if (entry.isDirectory() || entry.getName().startsWith("__MACOSX")) {
                        continue;
                    }

                    File fullPath = new File(zipBaseDir, entry.getName());

                    File parent = fullPath.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (InputStream is = zipFile.getInputStream(entry);
                         OutputStream os = new FileOutputStream(fullPath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                    }

                    if (!first) {
                        filePaths.append(", ");
                    }

                    filePaths.append(fullPath.getAbsolutePath());
                    first = false;
                }
            }

            if (filePaths.length() == 0) {
                throw new Exception("Zip contains no valid files to extract");
            }

            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(filePaths.toString());

            logger.info("Extracted file paths: " + filePaths);

            setSuccessMessage(
                    "Successfully extracted file paths from the zip and stored in runtime variable '"
                            + runTimeData.getKey() + "' with value: " + runTimeData.getValue()
            );

        } catch (RuntimeException e) {
            logger.warn("Unable to locate latest ZIP file " + e);
            setErrorMessage("Unable to find the latest zip file in downloads");
            result = Result.FAILED;

        } catch (Exception e) {
            logger.warn("ZIP processing failed " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to extract file paths from zip: " + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }

        return result;
    }
}