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

    public static void addStatusCodeData(Long runId, int statusCode, Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("statusCode", statusCode);
        saveAllData(runId, jsonObject, logger);
    }

    public static int getStatusCode(Long runId, Logger logger) throws Exception {
        logger.info("Getting status code for runId: " + runId);
        String encodedData = FileUtilities.readFromFile(runId);
        if (encodedData == null || encodedData.isEmpty()) {
            logger.info("Status code data is not present for runId: " + runId);
            throw new Exception("encodedData is not present for runId: " + runId);
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

}
