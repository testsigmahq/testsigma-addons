package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "fetch the data from the json file file-path and store it in the runtime variable variable-name",
        description = "This action stores JSON data into a runtime variable.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB
)
public class StoreJSONDataIntoRuntimeVariable extends WebAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        try {
            com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
            File jsonFileToUse = null;
            String inputFilePath = filePath.getValue().toString();
            if (inputFilePath.startsWith("http://") || inputFilePath.startsWith("https://")) {
                URL url = new URL(inputFilePath);
                String fileName = Paths.get(url.getPath()).getFileName().toString();
                File tempFile = File.createTempFile("downloaded-", fileName);
                try (InputStream in = url.openStream();
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                jsonFileToUse = tempFile;
            } else {
                jsonFileToUse = new File(inputFilePath);
            }
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                if (!jsonFileToUse.exists()) {
                    throw new FileNotFoundException("File not found: " + filePath);
                }
                // Read the file content as a String
                try (InputStream inputStream = new FileInputStream(jsonFileToUse)) {
                    logger.debug("Reading JSON file: " + jsonFileToUse.getAbsolutePath());
                    jsonData = mapper.readTree(inputStream).toString();
                    logger.debug("Successfully read JSON data from file: " + jsonFileToUse.getAbsolutePath());
                } catch (IOException e) {
                    throw new IOException("Error reading JSON file: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new Exception("Error processing JSON file: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.debug("Error while reading JSON file: " + ExceptionUtils.getStackTrace(e));
                setErrorMessage("Failed to read JSON file: " + e.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            }
            if(jsonData == null || jsonData.isEmpty()) {
                setErrorMessage("JSON data is empty or null.");
                return com.testsigma.sdk.Result.FAILED;
            }
            logger.debug("json data " + jsonData);
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(jsonData);
            logger.info("Stored JSON data in runtime variable: " + variableName.getValue().toString() +
                    " = " + jsonData);
            setSuccessMessage("Successfully stored JSON data in runtime variable: " +
                    variableName.getValue().toString() + " = " + jsonData);
            return result;
        } catch (Exception e) {
            logger.debug("Error while storing JSON data into runtime variable: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to store JSON data into runtime variable: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
