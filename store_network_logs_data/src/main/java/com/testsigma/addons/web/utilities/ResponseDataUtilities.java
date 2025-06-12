package com.testsigma.addons.web.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Base64;


public class ResponseDataUtilities {
    private static final Gson gson = new Gson();


    private static void saveAllData(Long runId, JsonObject jsonObject, com.testsigma.sdk.Logger logger) throws Exception {
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

    private static JsonArray getResponseBodyData(Long runId, com.testsigma.sdk.Logger logger) throws Exception {
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
            clearResponseDataByRunId(runId);
            logger.info("Failed to parse data for runId: " + runId + " with exception: " + e.getMessage());
            throw new Exception(e);
        }
        logger.info("Failed to get any data for runId: " + runId);
        throw new Exception("Failed to get any data for runId: " + runId);
    }


    public static void addResponseBodyData(Long runId, String responseBody, com.testsigma.sdk.Logger logger) throws Exception {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(responseBody);
        jsonObject.add("responseBody", jsonArray);
        saveAllData(runId, jsonObject, logger);
    }

    public static String getResponseBody(Long runId, com.testsigma.sdk.Logger logger) throws Exception {
        String responseBody = getResponseBodyData(runId, logger).getAsString();
        clearResponseDataByRunId(runId);
        return responseBody;
    }

}
