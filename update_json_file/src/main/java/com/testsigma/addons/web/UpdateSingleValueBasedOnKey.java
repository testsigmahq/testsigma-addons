package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Data
@Action(actionText = "JSON: Update the JSON file at File-Path by finding Key-Name with Key-Value, updating Key-To-Update to New-Value, and storing the file path in variable-name",
        description = "Updates a JSON file by finding an object with a specific key and value and then updates another key-value pair within the same object. Supports local file paths and URL paths. Stores file path in runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class UpdateSingleValueBasedOnKey extends WebAction {

    @TestData(reference = "File-Path")
    private com.testsigma.sdk.TestData filePathData;

    @TestData(reference = "Key-Name")
    private com.testsigma.sdk.TestData keyName;

    @TestData(reference = "Key-Value")
    private com.testsigma.sdk.TestData keyValue;

    @TestData(reference = "Key-To-Update")
    private com.testsigma.sdk.TestData keyToUpdateData;

    @TestData(reference = "New-Value")
    private com.testsigma.sdk.TestData newValueData;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData filePathVariable;


    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        String filePath = filePathData.getValue().toString();
        String keyName_ = keyName.getValue().toString();
        String keyValue_ = keyValue.getValue().toString();
        String keyToUpdate = keyToUpdateData.getValue().toString();
        String newValue = newValueData.getValue().toString();
        String variableName = filePathVariable.getValue().toString();
        File jsonFile = null;
        Path jsonPath = null;

        logger.info("Starting JSON Update process");
        logger.info("File Path: " + filePath);

        try {

            // Handle URL or local file
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                jsonFile = downloadFile(filePath);
                jsonPath = jsonFile.toPath();
            } else {
                jsonPath = Paths.get(filePath);
                jsonFile = jsonPath.toFile();
            }

            // Verify that the file exists
            if(!jsonFile.exists()){
                result = Result.FAILED;
                logger.warn("Error during JSON Update: File not found: "+ filePath);
                setErrorMessage("Error: File not found: " + filePath);
                return result;
            }

            String jsonString = new String(Files.readAllBytes(jsonPath));
            JsonNode json = objectMapper.readTree(jsonString);

            boolean updated = findAndUpdate(json, keyName_, keyValue_, keyToUpdate, newValue, false);

            if(!updated){
                result = Result.FAILED;
                logger.warn("Error during JSON Update to file: No object found to update");
                setErrorMessage("Error: No Object found to update.");
                return result;
            }

            // Write the updated JSON back to the SAME file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), json);

            String absoluteFilePath = jsonPath.toFile().getAbsolutePath();
            logger.info("JSON updated successfully in the original file." + absoluteFilePath);

            // Set runtime variable with the file path
            runTimeData.setKey(variableName);
            runTimeData.setValue(absoluteFilePath);

            setSuccessMessage("JSON updated successfully in file: " + absoluteFilePath);

        } catch (IOException e) {
            result = Result.FAILED;
            logger.warn("Error during JSON Update to file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error: File not found or unable to read file. " + e.getMessage());
        } finally{
            // Clean up downloaded file if needed.
            if(jsonFile != null && filePath.startsWith("http")) {
                jsonFile.delete();
            }
        }
        return result;
    }


    private boolean findAndUpdate(JsonNode json, String attributeName, String attributeValue, String keyToUpdate, String newValue, boolean found) {
        if (found) {
            return true; // Stop processing if already found
        }
        if (json.isObject()) {
            logger.info("Processing JSON Object: " + json.toString());
            ObjectNode jsonObject = (ObjectNode) json;
            Iterator<String> keys = jsonObject.fieldNames();

            while (keys.hasNext()) {
                String key = keys.next();
                JsonNode value = jsonObject.get(key);
                logger.info("  Checking key: " + key + ", Value: " + value.toString());
                if (key.equals(attributeName)) {
                    if (value != null && value.asText().equals(attributeValue)) {
                        logger.info("  Found attribute: " + attributeName + " with value " + attributeValue);
                        updateNestedKey(jsonObject, keyToUpdate, newValue);
                        return true; // Stop after finding and updating
                    }
                } else {
                    found = findAndUpdate(value, attributeName, attributeValue, keyToUpdate, newValue,found);
                    if (found) {
                        return true; // stop if found from child node
                    }
                }
            }
        } else if (json.isArray()) {
            logger.info("Processing JSON Array: " + json.toString());
            ArrayNode jsonArray = (ArrayNode) json;
            for (JsonNode jsonNode : jsonArray) {
                found =  findAndUpdate(jsonNode, attributeName, attributeValue, keyToUpdate, newValue,found);
                if (found) {
                    return true;
                }
            }
        }
        return found;
    }


    private void updateNestedKey(ObjectNode jsonObject, String keyToUpdate, String newValue) {
        Iterator<String> keys = jsonObject.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            JsonNode value = jsonObject.get(key);
            if (key.equals(keyToUpdate)) {
                logger.info("  Updating key " + keyToUpdate + " to " + newValue);
                jsonObject.put(keyToUpdate, newValue);
                return; // stop once found and updated
            }
            else if(value.isObject()){
                updateNestedKey((ObjectNode) value,keyToUpdate,newValue);
            }
        }

    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
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
        return tempFile;
    }
}