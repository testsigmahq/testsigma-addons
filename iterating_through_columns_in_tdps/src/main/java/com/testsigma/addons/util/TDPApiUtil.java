package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling TDP (Test Data Profile) API requests and JSON parsing
 */
public class TDPApiUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Makes an HTTP GET request to the TDP API and returns the response body
     * 
     * @param url The API URL
     * @param apiKey The Bearer token for authentication
     * @return The response body as a string
     * @throws Exception if the HTTP request fails
     */
    public static String makeHttpRequest(String url, String apiKey) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        
        // Set headers
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Bearer " + apiKey);
        
        // Make the API call
        CloseableHttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity());
        httpClient.close();
        
        return responseBody;
    }
    
    /**
     * Parses the TDP API response and extracts data for a specific iteration
     * 
     * @param responseBody The JSON response body from the TDP API
     * @param iterationName The name of the iteration to find (e.g., "iteration 1")
     * @return A map containing the parameter values for the specified iteration
     * @throws Exception if JSON parsing fails or iteration is not found
     */
    public static Map<String, String> parseTDPResponse(String responseBody, String iterationName) throws Exception {
        // Parse the JSON response
        JsonNode rootNode = objectMapper.readTree(responseBody);
        
        // Get the data array from the response
        JsonNode dataArray = rootNode.get("data");
        if (dataArray == null || !dataArray.isArray()) {
            throw new RuntimeException("No data array found in the response");
        }
        
        // Find the iteration with the matching name
        JsonNode matchingIteration = null;
        for (JsonNode iteration : dataArray) {
            String currentIterationName = iteration.get("name").asText();
            if (iterationName.equals(currentIterationName)) {
                matchingIteration = iteration;
                break;
            }
        }
        
        if (matchingIteration == null) {
            throw new RuntimeException("No iteration found with name: " + iterationName);
        }
        
        // Extract the data object from the matching iteration
        JsonNode iterationData = matchingIteration.get("data");
        if (iterationData == null) {
            throw new RuntimeException("No data found in the matching iteration");
        }
        
        // Store the parameter values
        Map<String, String> parameterValues = new HashMap<>();
        iterationData.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            String value = entry.getValue().asText();
            parameterValues.put(key, value);
        });
        
        return parameterValues;
    }
    
    /**
     * Convenience method that combines HTTP request and JSON parsing
     * 
     * @param tdpId The TDP ID
     * @param iterationName The name of the iteration to retrieve
     * @param apiKey The Bearer token for authentication
     * @return A map containing the parameter values for the specified iteration
     * @throws Exception if the request or parsing fails
     */
    public static Map<String, String> getTDPIterationData(String tdpId, String iterationName, String apiKey) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        String responseBody = makeHttpRequest(url, apiKey);
        return parseTDPResponse(responseBody, iterationName);
    }
}
