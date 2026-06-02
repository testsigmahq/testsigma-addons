package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@Action(actionText = "Replace text search-string with substitute-string at occurrence occurrence-index in file filepath-or-upload and store the updated file path in a runtime variable variable-name", description = "Replaces a specific occurrence of search-string with substitute-string in a file, and saves the new file path in a runtime variable", applicationType = ApplicationType.WEB, useCustomScreenshot = false)
public class ReplaceWordAtOccurrenceInFileWebAction extends WebAction {

    @TestData(reference = "search-string")
    private com.testsigma.sdk.TestData searchStringData;

    @TestData(reference = "substitute-string")
    private com.testsigma.sdk.TestData substituteStringData;

    @TestData(reference = "occurrence-index")
    private com.testsigma.sdk.TestData occurrenceIndexData;

    @TestData(reference = "filepath-or-upload")
    private com.testsigma.sdk.TestData filePathData;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating file text replacement at occurrence execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String searchStr = searchStringData.getValue().toString();
            String substituteStr = substituteStringData.getValue().toString();
            String filePathOrUrl = filePathData.getValue().toString();
            int occurrenceIndex = Integer.parseInt(occurrenceIndexData.getValue().toString());

            logger.debug("Search String: " + searchStr + ", Substitute String: "
                    + substituteStr + ", Occurrence: " + occurrenceIndex + ", File/URL Path: " + filePathOrUrl);

            File file = convertToFile(filePathOrUrl);
            Path path = file.toPath();

            if (!Files.exists(path)) {
                setErrorMessage("File not found at path: " + path.toString());
                return com.testsigma.sdk.Result.FAILED;
            }

            // Read the file content
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            // Replace specific instance of searchStr with substituteStr
            int index = -1;
            int count = 0;

            while (count < occurrenceIndex) {
                index = content.indexOf(searchStr, index + 1);
                if (index == -1) {
                    break;
                }
                count++;
            }

            if (index != -1) {
                String updatedContent = content.substring(0, index) + substituteStr
                        + content.substring(index + searchStr.length());
                // Write the updated content back to the file
                Files.write(path, updatedContent.getBytes(StandardCharsets.UTF_8));
                setSuccessMessage("Successfully replaced text '" + searchStr + "' with '" + substituteStr
                        + "' at occurrence " + occurrenceIndex + " and updated the file. File Path is "
                        + path.toAbsolutePath().toString());
            } else {
                setErrorMessage(
                        "The text '" + searchStr + "' does not occur " + occurrenceIndex + " times in the file.");
                return com.testsigma.sdk.Result.FAILED;
            }

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(path.toAbsolutePath().toString());

        } catch (NumberFormatException e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Occurrence-index provided is not a valid number: " + e.getMessage());
            setErrorMessage("Occurrence-index must be an integer number.");
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Error during text replacement: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error occurred during text replacement: " + ExceptionUtils.getMessage(e));
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            String originalFileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
            String extension = FilenameUtils.getExtension(originalFileName);
            String fileName = "file-" + java.util.UUID.randomUUID().toString()
                    + (extension.isEmpty() ? "" : "." + extension);
            logger.info("Given is a URL... File name: " + fileName);
            String filePath = FileUtils.getTempDirectoryPath() + File.separator + fileName;
            File tempFile = new File(filePath);
            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile);
            logger.info("Temp file created for URL: " + fileName + " at path " + filePath);
            return tempFile;
        } else {
            logger.info("Given is a local file path...");
            return new File(pathOrUrl);
        }
    }
}
