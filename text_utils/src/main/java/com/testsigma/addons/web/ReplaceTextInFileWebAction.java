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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(
        actionText = "Replace text between start-string and end-string with substitute-string in file filepath-or-upload and store the updated file path in a runtime variable variable-name",
        description = "Replaces text located between start-string and end-string with substitute-string in a file, and saves the new file path in a runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ReplaceTextInFileWebAction extends WebAction {

    @TestData(reference = "start-string")
    private com.testsigma.sdk.TestData startStringData;

    @TestData(reference = "end-string")
    private com.testsigma.sdk.TestData endStringData;

    @TestData(reference = "substitute-string")
    private com.testsigma.sdk.TestData substituteStringData;

    @TestData(reference = "filepath-or-upload")
    private com.testsigma.sdk.TestData filePathData;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating file text replacement execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String startStr = startStringData.getValue().toString();
            String endStr = endStringData.getValue().toString();
            String substituteStr = substituteStringData.getValue().toString();
            String filePathOrUrl = filePathData.getValue().toString();

            logger.debug("Start String: " + startStr +
                    ", End String: " + endStr +
                    ", Substitute String: " + substituteStr +
                    ", File/URL Path: " + filePathOrUrl);

            File file = convertToFile(filePathOrUrl);
            Path path = file.toPath();

            if (!Files.exists(path)) {
                setErrorMessage("File not found at path: " + path.toAbsolutePath());
                return com.testsigma.sdk.Result.FAILED;
            }

            // Read file content
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            // Regex to preserve spacing properly
            String regex = "(?s)(" + Pattern.quote(startStr) + ")(.*?)(" + Pattern.quote(endStr) + ")";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            StringBuffer updatedContent = new StringBuffer();

            while (matcher.find()) {
                String replacement = matcher.group(1) + " "
                        + Matcher.quoteReplacement(substituteStr) + " "
                        + matcher.group(3);
                matcher.appendReplacement(updatedContent, replacement);
            }
            matcher.appendTail(updatedContent);

            // Write updated content back
            Files.write(path, updatedContent.toString().getBytes(StandardCharsets.UTF_8));

            // Store updated file path in runtime variable
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(path.toAbsolutePath().toString());

            setSuccessMessage("Successfully replaced text between '" +
                    startStr + "' and '" + endStr +
                    "'. Updated File Path: " + path.toAbsolutePath());

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

            String fileName = "file-" + java.util.UUID.randomUUID()
                    + (extension.isEmpty() ? "" : "." + extension);

            logger.info("Given input is a URL. Temporary file name: " + fileName);

            String filePath = FileUtils.getTempDirectoryPath() + File.separator + fileName;
            File tempFile = new File(filePath);

            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile);

            logger.info("Temp file created at path: " + filePath);
            return tempFile;

        } else {
            logger.info("Given input is a local file path.");
            return new File(pathOrUrl);
        }
    }
}