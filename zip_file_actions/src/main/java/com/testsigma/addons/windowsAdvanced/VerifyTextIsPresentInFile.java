package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.ZipFileUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

@Data
@Action(actionText = "Verify if text text-to-verify is present in file file-name by extracting the zip file zip-file-path",
        description = "Extracts the specified zip file (supports local path or S3/HTTP URL), " +
                "searches for the given file within the extracted contents (supports exact name " +
                "or prefix match), and verifies whether the specified text or sentence is present " +
                "in that file. Extracted files are retained on disk after execution.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Verify if text is present in specified file by extracting the zip file",
        useCustomScreenshot = true)
public class VerifyTextIsPresentInFile extends WindowsAdvancedAction {

    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "file-name")
    private com.testsigma.sdk.TestData fileName;

    @TestData(reference = "zip-file-path")
    private com.testsigma.sdk.TestData zipFilePath;

    @Override
    protected Result execute() {
        logger.info("=== Verify Text In Zip File (Windows Advanced): Starting Execution ===");

        String searchText;
        String targetFileName;
        String zipPath;
        try {
            searchText = testData.getValue().toString().trim();
            targetFileName = fileName.getValue().toString().trim();
            zipPath = zipFilePath.getValue().toString().trim();
        } catch (Exception e) {
            setErrorMessage("Failed to read input parameters. Ensure text-to-verify, file-name, " +
                    "and zip-file-path are provided correctly. Error: " + e.getMessage());
            logger.info("Input parameter error: " + e.getMessage());
            return Result.FAILED;
        }

        ZipFileUtils.VerificationResult result =
                ZipFileUtils.verifyTextInZipFile(zipPath, targetFileName, searchText, logger);

        if (result.isSuccess()) {
            setSuccessMessage(result.getMessage());
            return Result.SUCCESS;
        } else {
            setErrorMessage(result.getMessage());
            return Result.FAILED;
        }
    }
}
