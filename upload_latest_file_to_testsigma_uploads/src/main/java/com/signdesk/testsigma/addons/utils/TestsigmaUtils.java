package com.signdesk.testsigma.addons.utils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.testsigma.sdk.Logger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestsigmaUtils {

  /**
   * Uploads a file to Testsigma uploads via the API and returns the raw Response.
   * Caller is responsible for closing the response inside a try-with-resources block.
   */
  public static Response uploadFile(String filePath, String projectId, String applicationId,
      String uploadName, String apiUrl, String apiKey) throws Exception {

    File pathFile = getLastModified(filePath);

    if (pathFile == null) {
      throw new IllegalArgumentException("No file found at path: " + filePath);
    }

    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    String version = String.valueOf(date.getTime() + calendar.getTimeInMillis());

    RequestBody body = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
            .addFormDataPart("fileContent", pathFile.getAbsolutePath().replaceAll("[/\\\\]", "_").replaceAll("^_+", ""),
            RequestBody.create(new File(pathFile.getAbsolutePath()), MediaType.parse("application/octet-stream")))
        .addFormDataPart("projectId", projectId)
        .addFormDataPart("name", uploadName)
        .addFormDataPart("uploadType", "Attachment")
        .addFormDataPart("platformType", "TestsigmaLab")
        .addFormDataPart("isPublic", "true")
        .addFormDataPart("applicationId", applicationId)
        .addFormDataPart("Version", version)
        .build();

    Request request = new Request.Builder()
        .url(apiUrl)
        .method("PUT", body)
        .addHeader("Authorization", "Bearer " + apiKey)
        .build();

    OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    return client.newCall(request).execute();
  }

  public static File getLastModified(String directoryFilePath) {
    File directory = new File(directoryFilePath);
    if (directory.isFile()) {
      return directory;
    }

    File[] files = directory.listFiles(File::isFile);
    long lastModifiedTime = Long.MIN_VALUE;
    File chosenFile = null;

    if (files != null) {
      for (File file : files) {
        if (file.lastModified() > lastModifiedTime) {
          chosenFile = file;
          lastModifiedTime = file.lastModified();
        }
      }
    }

    return chosenFile;
  }

  public static String readJsonData(String jsonString, String jsonPath, Logger logger) throws PathNotFoundException {
    logger.info("Reading JSON data with path: " + jsonPath);

    jsonString = preprocessJsonString(jsonString, logger);

    if (jsonString == null || jsonString.isEmpty()) {
      String errorMsg = "JSON string is null or empty";
      logger.warn(errorMsg);
      throw new IllegalArgumentException(errorMsg);
    }

    try {
      Object result = JsonPath.read(jsonString, jsonPath);
      String output = result.toString();
      logger.info("Successfully extracted data using JSON path: " + jsonPath);
      return output;
    } catch (PathNotFoundException e) {
      String errorMsg = "Invalid JSON Path: " + jsonPath + ". Path not found in JSON structure.";
      logger.warn(errorMsg);
      throw new PathNotFoundException(errorMsg);
    } catch (Exception e) {
      String errorMsg = "Error reading JSON data with path " + jsonPath + ": " + e.getMessage();
      logger.warn(errorMsg);
      throw new RuntimeException(errorMsg, e);
    }
  }

  public static String preprocessJsonString(String jsonString, Logger logger) {
    if (jsonString == null) {
      logger.warn("JSON string is null, returning null");
      return null;
    }

    String cleaned = jsonString
        .replaceAll("\\u00A0", " ")
        .replaceAll("\\u2007", " ")
        .replaceAll("\\u202F", " ")
        .replaceAll("\\u2060", "")
        .replaceAll("\\uFEFF", "")
        .trim();

    cleaned = cleaned.replaceAll("\\s+", " ");

    logger.info("Preprocessed JSON string: removed problematic characters and normalized whitespace");

    return cleaned;
  }
}
