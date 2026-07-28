package com.testsigma.addons.web.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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


    private static JsonArray getResponseBodyData(Long runId, Logger logger) throws Exception {
        logger.info("Getting all data for runId: " + runId);
        String encodedData = FileUtilities.readFromFile(runId);
        if (encodedData == null || encodedData.isEmpty()) {
            logger.info("Data is not present for runId: " + runId);
            return new JsonArray();
        }

        String json = new String(Base64.getDecoder().decode(encodedData));
        try {
            JsonObject data = gson.fromJson(json, JsonObject.class);
            if (data != null && data.has("responseBody")) {
                logger.info("Got all data for runId: " + runId + ", data: " + data);
                return data.getAsJsonArray("responseBody");
            }
        } catch (JsonParseException e) {

            logger.info("Failed to parse data for runId: " + runId + " with exception: " + e.getMessage());
            throw new Exception(e);
        }
        logger.info("Failed to get any data for runId: " + runId);
        throw new Exception("Failed to get any data for runId: " + runId);
    }


    public static void addResponseBodyData(Long runId, String responseBody, Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(responseBody);
        jsonObject.add("responseBody", jsonArray);
        saveAllData(runId, jsonObject, logger);
    }

    public static String getResponseBody(Long runId, Logger logger) throws Exception {
        String responseBody = getResponseBodyData(runId, logger).getAsString();

        return responseBody;
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

            logger.info("Failed to parse request headers data for runId: " + runId + " with exception: " + e.getMessage());
            throw new Exception(e);
        }
        logger.info("Failed to get any request headers data for runId: " + runId);
        throw new Exception("Failed to get any request headers data for runId: " + runId);
    }

    public static void addRequestHeadersData(Long runId, String requestHeaders, Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(requestHeaders);
        jsonObject.add("requestHeaders", jsonArray);
        saveAllData(runId, jsonObject, logger);
    }

    public static void addStatusCodeAndRequestHeadersData(Long runId, int statusCode, String requestHeaders, Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("statusCode", statusCode);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(requestHeaders);
        jsonObject.add("requestHeaders", jsonArray);
        saveAllData(runId, jsonObject, logger);
    }

    public static void addAllNetworkData(Long runId, int statusCode, String requestHeaders, String responseBody, Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("statusCode", statusCode);

        JsonArray requestHeadersArray = new JsonArray();
        requestHeadersArray.add(requestHeaders);
        jsonObject.add("requestHeaders", requestHeadersArray);

        JsonArray responseBodyArray = new JsonArray();
        responseBodyArray.add(responseBody);
        jsonObject.add("responseBody", responseBodyArray);

        saveAllData(runId, jsonObject, logger);
    }

    public static void saveAllNetworkData(Long runId, JsonObject allData, Logger logger) throws Exception {
        saveAllData(runId, allData, logger);
    }

    public static void savePayloadData(Long runId, String payload, Logger logger) throws Exception {
        try {
            logger.info("Saving payload to separate file for runId: " + runId);
            FileUtilities.writePayloadToFile(runId, payload);
            logger.info("Payload saved successfully for runId: " + runId);
        } catch (Exception e) {
            logger.info("Failed to save payload for runId: " + runId + " - " + e.getMessage());
            throw new Exception(e);
        }
    }

    public static String getRequestHeaders(Long runId, Logger logger) throws Exception {
        String requestHeaders = getRequestHeadersData(runId, logger).getAsString();

        return requestHeaders;
    }

    public static String getSpecificHeaderValue(Long runId, String headerKey, Logger logger) throws Exception {
        logger.info("Getting specific header value for key: " + headerKey + " from runId: " + runId);
        JsonArray headersArray = getRequestHeadersData(runId, logger);

        if (headersArray.size() == 0) {
            throw new Exception("No headers found for runId: " + runId);
        }

        String headersString = headersArray.get(0).getAsString();
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

        throw new Exception("Header '" + headerKey + "' not found in request headers");
    }


    public static void addStatusCodeData(Long runId, int statusCode, Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("statusCode", statusCode);
        saveAllData(runId, jsonObject, logger);
    }

    public static int getStatusCode(Long runId, Logger logger) throws Exception {
        int statusCode = getStatusCodeData(runId, logger);
        if (statusCode == -1) {
            logger.info("No status code found for runId: " + runId);
            throw new Exception("No status code found for runId: " + runId);
        }
        return statusCode;
    }

    public static int getStatusCodeData(Long runId, Logger logger) throws Exception {
        logger.info("Getting status code for runId: " + runId);
        String encodedData = FileUtilities.readFromFile(runId);
        if (encodedData == null || encodedData.isEmpty()) {
            logger.info("The file has no data to fetch for the run id: " + runId);
            return -1;
        }

        String json = new String(Base64.getDecoder().decode(encodedData));
        try {
            JsonObject data = gson.fromJson(json, JsonObject.class);
            if (data != null && data.has("statusCode")) {
                int statusCode = data.get("statusCode").getAsInt();
                logger.info("Got status code for runId: " + runId + ", statusCode: " + statusCode);
                return statusCode;
            }
        } catch (JsonParseException e) {

            logger.info("Failed to parse status code data for runId: " + runId + " with exception: " + e.getMessage());
            throw new Exception(e);
        }
        logger.info("Failed to get any status code for runId: " + runId);
        throw new Exception("Failed to get any status code for runId: " + runId);
    }

    public static JsonArray getPayloadData(Long runId, Logger logger) throws Exception {
        logger.info("Getting payload data for runId: " + runId);
        String payload = FileUtilities.readPayloadFromFile(runId);
        if (payload == null || payload.isEmpty()) {
            logger.info("Payload data is not present for runId: " + runId);
            return new JsonArray();
        }
        logger.info("Got payload data for runId: " + runId + ", payload: " + payload);
        try {
            JsonElement parsed = gson.fromJson(payload, JsonElement.class);
            if (parsed.isJsonArray()) {
                return parsed.getAsJsonArray();
            }
            JsonArray result = new JsonArray();
            result.add(parsed);
            return result;
        } catch (JsonParseException e) {
            JsonArray result = new JsonArray();
            result.add(payload);
            return result;
        }
    }

    public static int getResponseTime(Long runId, Logger logger) throws Exception {
        logger.info("Getting response time for runId: " + runId);
        String encodedData = FileUtilities.readFromFile(runId);
        if (encodedData == null || encodedData.isEmpty()) {
            logger.info("Response time data is not present for runId: " + runId);
            throw new Exception("Failed to get any response time for runId: " + runId);

        }

        String json = new String(Base64.getDecoder().decode(encodedData));
        try {
            JsonObject data = gson.fromJson(json, JsonObject.class);
            if (data != null && data.has("responseTime")) {
                int responseTime = data.get("responseTime").getAsInt();
                logger.info("Got response time for runId: " + runId + ", responseTime: " + responseTime);
                return responseTime;
            }
        } catch (JsonParseException e) {

            logger.info("Failed to parse response time data for runId: " + runId + " with exception: " + e.getMessage());
            throw new Exception(e);
        }
        logger.info("Failed to get any response time for runId: " + runId);
        throw new Exception("Failed to get any response time for runId: " + runId);


    }
}
