package com.testsigma.addons.web.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.testsigma.sdk.Logger;

import java.util.Base64;


public class ResponseDataUtilities {
    private static final Gson gson = new Gson();


    private static void saveAllData(Long runId, JsonObject jsonObject, Logger logger) throws Exception {
        try {
            logger.info("Saving data for the current testcase " + jsonObject);
            String json = gson.toJson(jsonObject);
            String encodedData = Base64.getEncoder().encodeToString(json.getBytes());
            FileUtilities.writeToFile(runId, encodedData);
        } catch (Exception e) {
            logger.info("Did not saved data for the current testcase " + e.getMessage());
            throw new Exception(e);
        }
    }

    public static void clearResponseDataByRunId(Long runId) throws Exception {
        FileUtilities.deleteFile(runId);
    }

    public static JsonArray getRequestHeadersData(Long runId, Logger logger) throws Exception {
        logger.info("Getting all request headers data for runId: " + runId);
        String encodedData = FileUtilities.readFromFile(runId);
        if (encodedData == null || encodedData.isEmpty()) {
            logger.info("Request headers data is not present for runId: " + runId);
            return new JsonArray();
        }

        String json = new String(Base64.getDecoder().decode(encodedData));
//        logger.info("Decoded request headers data for runId: " + runId + ", json: " + json);
        try {
            JsonObject data = gson.fromJson(json, JsonObject.class);
            if (data != null && data.has("requestHeaders")) {
                logger.info("request headers : " + data.getAsJsonArray("requestHeaders"));
                return data.getAsJsonArray("requestHeaders");
            } else{
                logger.info("No request headers data found for runId: " + runId);
            }
        } catch (JsonParseException e) {
            clearResponseDataByRunId(runId);
            logger.info("Failed to parse request headers data for runId: " + runId + " with exception: " + e.getMessage());
            throw new Exception(e);
        }
        logger.info("Failed to get any request headers data for runId: " + runId);
        throw new Exception("Failed to get any request headers data for runId: " + runId);
    }

    public static void saveAllNetworkData(Long runId, JsonObject allData, Logger logger) throws Exception {
        saveAllData(runId, allData, logger);
    }

    public static String getSpecificHeaderValue(Long runId, String headerKey, Logger logger) throws Exception {
        logger.info("Getting specific header value for key: " + headerKey + " from runId: " + runId);
        JsonArray headersArray = getRequestHeadersData(runId, logger);

        if (headersArray.size() == 0) {
            throw new Exception("No headers found for runId: " + runId);
        }

        // Get the first element from the array
        var headerElement = headersArray.get(0);
        logger.info("Header element type: " + headerElement.getClass().getSimpleName());

        // Check if it's a JSON object (which is the case based on the logs)
        if (headerElement.isJsonObject()) {
            JsonObject headersObject = headerElement.getAsJsonObject();
            logger.info("Headers object: " + headersObject);
            
            // Look for the header key (case-insensitive)
            for (String key : headersObject.keySet()) {
                if (key.equalsIgnoreCase(headerKey)) {
                    String value = headersObject.get(key).getAsString();
                    logger.info("Found matching header: " + key + " = " + value);
                    return value;
                }
            }
        } else if (headerElement.isJsonPrimitive()) {
            // Fallback for string format (legacy support)
            String headersString = headerElement.getAsString();
            logger.info("Headers string: " + headersString);

            // Parse the headers string (format: "key1: value1\nkey2: value2\n...")
            String[] headerLines = headersString.split("\n");
            for (String headerLine : headerLines) {
                if (headerLine.trim().isEmpty()) continue;

                int colonIndex = headerLine.indexOf(": ");
                if (colonIndex > 0) {
                    String key = headerLine.substring(0, colonIndex).trim();
                    String value = headerLine.substring(colonIndex + 2).trim();

                    logger.info("Checking header - Key: " + key + ", Value: " + value);

                    if (key.equalsIgnoreCase(headerKey)) {
                        logger.info("Found matching header: " + key + " = " + value);
                        return value;
                    }
                }
            }
        }

        throw new Exception("Header '" + headerKey + "' not found in request headers");
    }

}
