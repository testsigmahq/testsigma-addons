package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.stream.Stream;


@Data
@Action(actionText = "Get the name of the latest file with the name filename from downloads-directory" +
        "(on the local machine) and store it in the runtime variable filepath.",
        description = "Finds the most recent file name with the specified name on the local " +
                "machine and saves its path to the filepath variable.",
        applicationType = ApplicationType.WEB)
public class GetLatestFileNameFromDownloads extends WebAction {

    @TestData(reference = "filename")
    private com.testsigma.sdk.TestData filename;
    @TestData(reference = "downloads-directory")
    private com.testsigma.sdk.TestData downloadsDirectory;
    @TestData(reference = "filepath", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData filepath;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        final String fileNameToBeSearched = filename.getValue().toString();
        final String FAILED_MESSAGE = "File with name " + fileNameToBeSearched + "not found. ";
        try {
            logger.info("Searching for file");

            String downloadsPath = downloadsDirectory.getValue().toString();
            logger.info("Downloading file: " + downloadsPath);
            try (Stream<Path> files = Files.list(Path.of(downloadsPath))) {
                // Filter files based on name and extension and sort by last modified time, newest first
                Path newestFile = files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith(fileNameToBeSearched))
                        .max(Comparator.comparingLong(GetLatestFileNameFromDownloads::getLastModifiedTime))
                        .orElse(null);
                logger.info("latest Path:");

                if (newestFile != null) {
                    logger.info("File found, latest Path is " + newestFile.getFileName());
                    runTimeData.setValue(newestFile.getFileName().toString());
                    runTimeData.setKey(filepath.getValue().toString());
                    setSuccessMessage("Found most recent file path with the specified name : " + newestFile.getFileName().toString());
                } else {
                    logger.info("file path is null");
                    setErrorMessage(FAILED_MESSAGE);
                    result = Result.FAILED;
                }
            }
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage(FAILED_MESSAGE + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }

    private static long getLastModifiedTime(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toMillis();
        } catch (IOException e) {
            return 0;
        }
    }
}
