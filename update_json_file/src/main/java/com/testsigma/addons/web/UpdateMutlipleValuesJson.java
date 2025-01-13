package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

@Data
@Action(actionText = "Update all occurrences of the value for the key key-to-update in the JSON file filepath with the new value new-value, and store the path of the modified file in the runtime variable variable-name",
        description = "Updates the value of a given key in a JSON file and stores the modified file path in the given runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class UpdateMutlipleValuesJson extends WebAction {

  @TestData(reference = "filepath")
  private com.testsigma.sdk.TestData jsonFile;

  @TestData(reference = "key-to-update")
  private com.testsigma.sdk.TestData keyToUpdate;

  @TestData(reference = "new-value")
  private com.testsigma.sdk.TestData newValue;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variable;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String filePath = jsonFile.getValue().toString();
    String key = keyToUpdate.getValue().toString();
    String value = newValue.getValue().toString();
    File jsonFileToUse = null;

    try {

      // Check if the path is a URL
      if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        jsonFileToUse = downloadFile(filePath);
      } else {
        jsonFileToUse = new File(filePath);
      }


      String modifiedFilePath = updateJsonValue(jsonFileToUse.getAbsolutePath(), key,value);
      runTimeData.setKey(variable.getValue().toString());
      runTimeData.setValue(modifiedFilePath);
      setSuccessMessage("JSON updated successfully! , Updated key "+key+" with value "+value+". Modified file path is stored in runtime variable "+variable.getValue().toString() + "=" + modifiedFilePath);
      logger.info("Modified file path" + modifiedFilePath);
      logger.info("File updated successfully");

      if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        jsonFileToUse.delete();
      }

    } catch (IOException e) {
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("Failed to update JSON: " + e.getMessage() +" File Path "+filePath+" , Updated key "+key+" with value "+value);
      logger.warn("Failed to update JSON: " + e.getMessage() + e);
    }
    return result;
  }

  public String updateJsonValue(String filePath, String key, String newValue) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    File file = new File(filePath);
    JsonNode rootNode = mapper.readTree(file);

    boolean isUpdated = updateKeyValue(rootNode, key, newValue);
    String modifiedFilePath = filePath;


    if (isUpdated) {
      // Write the updated JSON back to the original file
      mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
      logger.info("Key \"" + key + "\" updated successfully in place: " + filePath);
    } else {
      logger.warn("Key \"" + key + "\" not found in the JSON file.");
      throw new IOException("Key \"" + key + "\" not found in the JSON file.");
    }
    return modifiedFilePath;
  }

  private boolean updateKeyValue(JsonNode node, String key, String newValue) {
    boolean isUpdated = false;

    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();

        if (field.getKey().equals(key)) {
          objectNode.put(key, newValue);
          isUpdated = true;
        }

        if (field.getValue().isObject() || field.getValue().isContainerNode()) {
          boolean updatedInNested = updateKeyValue(field.getValue(), key, newValue);
          isUpdated = isUpdated || updatedInNested;
        }
      }
    } else if (node.isArray()) {

      for (JsonNode arrayElement : node) {
        boolean updatedInArray = updateKeyValue(arrayElement, key, newValue);
        isUpdated = isUpdated || updatedInArray;
      }
    }
    logger.info("Updated key: " + key + " with value " + newValue);
    return isUpdated;
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

