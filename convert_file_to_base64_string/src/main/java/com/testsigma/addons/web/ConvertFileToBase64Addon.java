package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Data
@Action(
        actionText = "Convert file from path filepath-or-upload to Base64 String and store it in runtime variable variable-name",
        description = "Converts a file into Base64 encoded string and stores it in a runtime variable",
        applicationType = ApplicationType.WEB
)
public class ConvertFileToBase64Addon extends WebAction {

    @TestData(reference = "filepath-or-upload")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution.....");
        Result result =  Result.SUCCESS;
        try {
            File file = convertToFile(filePath.getValue().toString());
            logger.info("File path is " + file.getAbsolutePath());
            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            String base64Output = Base64.getEncoder().encodeToString(fileBytes);
            logger.info("Base64 encoded string is: " + base64Output);

            runTimeData.setValue(base64Output);
            runTimeData.setKey(variableName.getValue().toString());
            setSuccessMessage("File successfully converted to Base64 and stored in runtime variable." + variableName.getValue().toString() + " = " + base64Output);

        } catch (Exception e) {
            logger.warn("Failed to convert file to Base64. Error: "
                    + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to convert file to Base64. Error: "
                    + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            String fileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
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
