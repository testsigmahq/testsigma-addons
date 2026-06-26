package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.sdk.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for handling TDP (Test Data Profile) API requests and JSON parsing
 */
public class TDPApiUtil {
    Logger logger;

    TDPApiUtil(Logger logger) {
        this.logger = logger;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Makes an HTTP GET request to the TDP API and returns the response body
     *
     * @param url    The API URL
     * @param apiKey The Bearer token for authentication
     * @return The response body as a string
     * @throws Exception if the HTTP request fails
     */
    public static String makeHttpRequest(String url, String apiKey, Logger logger) throws Exception {
        logger.info("Preparing to make HTTP GET request to URL: " + url);
        // Create SSL context that accepts all certificates
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial((chain, authType) -> true)
                .build();
        logger.info("SSL context created to trust all certificates");
        // Create SSL socket factory with no hostname verification
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE
        );
        logger.info("SSL socket factory created with NoopHostnameVerifier");

        // Configure request with timeouts (10 seconds for connect, 30 seconds for socket, 60 seconds for connection request)
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)  // 10 seconds
                .setSocketTimeout(30000)   // 30 seconds
                .setConnectionRequestTimeout(60000)  // 60 seconds
                .build();
        logger.info("Request configuration set with timeouts");
        // Create HttpClient with SSL verification disabled
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultRequestConfig(requestConfig)
                .build();
        logger.info("HttpClient created with custom SSL socket factory and request configuration");
        HttpGet httpGet = new HttpGet(url);

        // Set headers
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Bearer " + apiKey);
        logger.info("HTTP GET request headers set");
        logger.info("httpGet Details: " + httpGet);
        logger.info("Headers" + Arrays.toString(httpGet.getAllHeaders()));
        // Make the API call
        try {
            logger.info("Executing HTTP GET request");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            logger.info("HTTP GET request executed, received response with status code: "
                    + response.getStatusLine().getStatusCode());
            String responseBody = EntityUtils.toString(response.getEntity());
            logger.info("Response body received");
            return responseBody;
        } catch (Exception e) {
            logger.info("Error occurred while making HTTP request: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Error occurred while making HTTP request: " + e.getMessage(), e);
        } finally {
            logger.info("Closing HttpClient");
            httpClient.close();
        }
    }


    public static String makeHttpRequest2(String requestUrl, String apikey, Logger logger) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        String responseBody = null;
        try {
            //trust certificates
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
            newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            newBuilder.hostnameVerifier((hostname, session) -> true);


            OkHttpClient newClient = newBuilder.build();

            // log the request details
            logger.info("Making HTTP GET request to URL: " + requestUrl);
            // add headers
            Request request= new Request.Builder()
                    .url(requestUrl)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apikey)
                    .build();

            logger.info("Request Details: " + request);
            okhttp3.Response response = newClient.newCall(request).execute();

            // get response code
            logger.info("Response code: " + response.code());

            responseBody = response.body().string();
            logger.info("Response body: " + responseBody);
        } catch (Exception e) {
            logger.info("Error occurred while making HTTP request: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
        return responseBody;


    }

    /**
     * Parses the TDP API response and extracts data for a specific iteration
     *
     * @param responseBody  The JSON response body from the TDP API
     * @param iterationName The name of the iteration to find (e.g., "iteration 1")
     * @return A map containing the parameter values for the specified iteration
     * @throws Exception if JSON parsing fails or iteration is not found
     */
    public static Map<String, String> parseTDPResponse(String responseBody, String iterationName, Logger logger) throws Exception {
        // Parse the JSON response
        logger.info("Parsing TDP API response to extract iteration data for: " + iterationName);
        JsonNode rootNode = objectMapper.readTree(responseBody);

        logger.info("JSON response parsed successfully");
        // Get the data array from the response
        JsonNode dataArray = rootNode.get("data");
        if (dataArray == null || !dataArray.isArray()) {
            throw new RuntimeException("No data array found in the response");
        }
        logger.info("Data array found in the response with " + dataArray.size() + " iterations");
        // Find the iteration with the matching name
        logger.info("Searching for iteration with name: " + iterationName);
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
        logger.info("Matching iteration found: " + iterationName);
        // Extract the data object from the matching iteration
        JsonNode iterationData = matchingIteration.get("data");
        if (iterationData == null) {
            throw new RuntimeException("No data found in the matching iteration");
        }
        logger.info("Extracting parameter values from the iteration data");

        // Get the columns array from root node to preserve order
        JsonNode columnsArray = rootNode.get("columns");
        if (columnsArray == null || !columnsArray.isArray()) {
            logger.warn("No columns array found in response, falling back to default iteration order");
        }

        // Store the parameter values in LinkedHashMap to preserve insertion order
        Map<String, String> parameterValues = new LinkedHashMap<>();

        if (columnsArray != null && columnsArray.isArray()) {
            // Iterate in the order specified by the columns array
            logger.info("Iterating columns in the order specified by columns array");
            for (JsonNode columnNode : columnsArray) {
                String columnName = columnNode.asText();
                JsonNode columnValue = iterationData.get(columnName);
                if (columnValue != null && !columnValue.isNull()) {
                    // add only those values which have data within it, ignore columns with null values
                    if (columnValue.asText().trim().isEmpty()) {
                        logger.debug("Skipping column: " + columnName + " as it has empty value");
                        continue;
                    }
                    String value = columnValue.asText();
                    parameterValues.put(columnName, value);
                    logger.debug("Added column: " + columnName + " = " + value);
                }
            }
        } else {
            // Fallback: if columns array is not available, use the order from iterationData
            logger.info("Using default iteration order from data object");
            iterationData.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String value = entry.getValue().asText();
                parameterValues.put(key, value);
            });
        }

        logger.info("Parameter values extracted successfully in column order: " + parameterValues);

        return parameterValues;
    }

    /**
     * Convenience method that combines HTTP request and JSON parsing
     *
     * @param tdpId         The TDP ID
     * @param iterationName The name of the iteration to retrieve
     * @param apiKey        The Bearer token for authentication
     * @return A map containing the parameter values for the specified iteration
     * @throws Exception if the request or parsing fails
     */
    public static Map<String, String> getTDPIterationData(String tdpId, String iterationName, String apiKey, Logger logger) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        logger.info("Making request to TDP API URL: " + url);
        String responseBody = makeHttpRequest2(url, apiKey, logger);
        logger.info("Received response from TDP API");
        return parseTDPResponse(responseBody, iterationName, logger);
    }

    /**
     * Makes an HTTP request with a JSON body using OkHttp.
     * Supports PUT, POST, and PATCH methods.
     *
     * @param url      The API URL
     * @param method   The HTTP method (PUT, POST, PATCH)
     * @param jsonBody The JSON body to send
     * @param apiKey   The Bearer token for authentication
     * @param logger   Logger instance
     * @return The response body as a string
     * @throws Exception if the request fails or returns a non-2xx status
     */
    public static String makeHttpRequestWithBody(String url, String method, String jsonBody,
                                                  String apiKey, Logger logger) throws Exception {
        logger.info("Preparing " + method + " request to URL: " + url);
        logger.info("Request body: " + jsonBody);

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        clientBuilder.hostnameVerifier((hostname, session) -> true);
        OkHttpClient client = clientBuilder.build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey);

        switch (method.toUpperCase()) {
            case "PUT":
                requestBuilder.put(body);
                break;
            case "POST":
                requestBuilder.post(body);
                break;
            case "PATCH":
                requestBuilder.patch(body);
                break;
            case "DELETE":
                requestBuilder.delete(body);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        okhttp3.Response response = client.newCall(requestBuilder.build()).execute();
        String responseBody = response.body() != null ? response.body().string() : "";
        logger.info("Response code: " + response.code());
        logger.info("Response body: " + responseBody);

        if (!response.isSuccessful()) {
            throw new RuntimeException("HTTP " + response.code() + ": " + responseBody);
        }
        return responseBody;
    }

    /**
     * Updates the parameter values for a specific set (row) in a TDP.
     * Uses PATCH to merge updated data into the existing TDP.
     * The PATCH endpoint matches rows by name and updates them.
     *
     * @param tdpId       The TDP ID
     * @param setName     The name of the set/row to update
     * @param updatedData A map of parameter names to their new values
     * @param apiKey      The Bearer token for authentication
     * @param logger      Logger instance
     * @return The API response as a string
     * @throws Exception if the request or parsing fails
     */
    public static String updateTDPIterationData(String tdpId, String setName,
                                                 Map<String, String> updatedData,
                                                 String apiKey, Logger logger) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        logger.info("Updating TDP " + tdpId + " set '" + setName + "' with data: " + updatedData);

        ObjectNode dataObj = objectMapper.createObjectNode();
        for (Map.Entry<String, String> entry : updatedData.entrySet()) {
            dataObj.put(entry.getKey(), entry.getValue());
        }

        ObjectNode setObj = objectMapper.createObjectNode();
        setObj.put("name", setName);
        setObj.set("data", dataObj);

        ArrayNode dataArray = objectMapper.createArrayNode();
        dataArray.add(setObj);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("data", dataArray);

        return makeHttpRequestWithBody(url, "PATCH", objectMapper.writeValueAsString(requestBody), apiKey, logger);
    }

    /**
     * Adds a new row (set) to an existing TDP.
     * Fetches the current TDP, appends the new row, and PUTs the full data back.
     * This avoids the PATCH endpoint's limitation where mergeDataSet is bounded by
     * min(existingRows, requestRows), making it unreliable for adding rows.
     *
     * @param tdpId   The TDP ID
     * @param rowName The name for the new row/set (e.g., "Set6")
     * @param rowData A map of parameter names to their values for the new row
     * @param apiKey  The Bearer token for authentication
     * @param logger  Logger instance
     * @return The API response as a string
     * @throws Exception if the request or parsing fails
     */
    public static String addTDPRow(String tdpId, String rowName, Map<String, String> rowData,
                                    String apiKey, Logger logger) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        logger.info("Adding new row '" + rowName + "' to TDP " + tdpId);

        String existingResponse = makeHttpRequest2(url, apiKey, logger);
        JsonNode existing = objectMapper.readTree(existingResponse);

        JsonNode existingDataNode = existing.get("data");
        if (existingDataNode == null || !existingDataNode.isArray()) {
            throw new RuntimeException("No data array found in existing TDP response");
        }
        ArrayNode existingData = (ArrayNode) existingDataNode;

        ObjectNode newRowDataNode = objectMapper.createObjectNode();
        for (Map.Entry<String, String> entry : rowData.entrySet()) {
            newRowDataNode.put(entry.getKey(), entry.getValue());
        }

        ObjectNode newSet = objectMapper.createObjectNode();
        newSet.put("name", rowName);
        newSet.put("expectedToFail", false);
        newSet.put("position", existingData.size());
        newSet.set("data", newRowDataNode);

        existingData.add(newSet);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("testDataName", existing.get("testDataName").asText());
        requestBody.set("data", existingData);

        return makeHttpRequestWithBody(url, "PUT", objectMapper.writeValueAsString(requestBody), apiKey, logger);
    }

    /**
     * Adds a new row with empty values for all existing parameters.
     * Reads column names from the first existing row and sets them all to empty strings.
     *
     * @param tdpId   The TDP ID
     * @param rowName The name for the new row/set
     * @param apiKey  The Bearer token for authentication
     * @param logger  Logger instance
     * @return The API response as a string
     * @throws Exception if the request or parsing fails
     */
    public static String addTDPRowWithEmptyData(String tdpId, String rowName,
                                                 String apiKey, Logger logger) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        logger.info("Adding new row '" + rowName + "' with empty values to TDP " + tdpId);

        String existingResponse = makeHttpRequest2(url, apiKey, logger);
        JsonNode existing = objectMapper.readTree(existingResponse);

        JsonNode existingDataNode = existing.get("data");
        if (existingDataNode == null || !existingDataNode.isArray() || existingDataNode.size() == 0) {
            throw new RuntimeException("No existing data/rows found in TDP to derive column names");
        }
        ArrayNode existingData = (ArrayNode) existingDataNode;

        ObjectNode newRowDataNode = objectMapper.createObjectNode();
        JsonNode firstRowData = existingData.get(0).get("data");
        if (firstRowData != null && firstRowData.isObject()) {
            firstRowData.fieldNames().forEachRemaining(column -> newRowDataNode.put(column, ""));
        }
        logger.info("Created empty row with columns: " + newRowDataNode.fieldNames());

        ObjectNode newSet = objectMapper.createObjectNode();
        newSet.put("name", rowName);
        newSet.put("expectedToFail", false);
        newSet.put("position", existingData.size());
        newSet.set("data", newRowDataNode);

        existingData.add(newSet);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("testDataName", existing.get("testDataName").asText());
        requestBody.set("data", existingData);

        return makeHttpRequestWithBody(url, "PUT", objectMapper.writeValueAsString(requestBody), apiKey, logger);
    }

    /**
     * Returns the total number of rows (sets/iterations) in a TDP.
     *
     * @param tdpId  The TDP ID
     * @param apiKey The Bearer token for authentication
     * @param logger Logger instance
     * @return The number of rows in the TDP
     * @throws Exception if the request or parsing fails
     */
    public static int getTDPRowCount(String tdpId, String apiKey, Logger logger) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        logger.info("Fetching row count for TDP: " + tdpId);
        String responseBody = makeHttpRequest2(url, apiKey, logger);
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode dataArray = rootNode.get("data");
        if (dataArray == null || !dataArray.isArray()) {
            throw new RuntimeException("No data array found in the response");
        }
        int rowCount = dataArray.size();
        logger.info("Total row count for TDP " + tdpId + ": " + rowCount);
        return rowCount;
    }

    /**
     * Replaces the target TDP with an exact replica of the reference TDP.
     * Fetches the reference TDP in full and PUTs the entire payload (testDataName,
     * columns, data) directly to the target — completely overwriting whatever was there.
     *
     * @param referenceTdpId The source TDP ID to copy from
     * @param targetTdpId    The destination TDP ID to fully replace
     * @param apiKey         The Bearer token for authentication
     * @param logger         Logger instance
     * @return The API response as a string
     * @throws Exception if the request or parsing fails
     */
    public static String copyTDPRows(String referenceTdpId, String targetTdpId,
                                      String apiKey, Logger logger) throws Exception {
        String baseUrl = "https://app.testsigma.com/api/v1/test_data/";
        logger.info("Replacing target TDP " + targetTdpId + " with exact replica of reference TDP " + referenceTdpId);

        String referenceResponse = makeHttpRequest2(baseUrl + referenceTdpId, apiKey, logger);
        JsonNode referenceJson = objectMapper.readTree(referenceResponse);

        String targetResponse = makeHttpRequest2(baseUrl + targetTdpId, apiKey, logger);
        JsonNode targetJson = objectMapper.readTree(targetResponse);

        // Delete all existing rows in the target before inserting new ones.
        // PUT's saveAll only inserts rows without IDs — it never removes old rows,
        // so running the action multiple times would accumulate duplicates.
        JsonNode targetDataNode = targetJson.get("data");
        if (targetDataNode != null && targetDataNode.isArray() && targetDataNode.size() > 0) {
            StringBuilder ids = new StringBuilder();
            for (JsonNode row : targetDataNode) {
                if (row.has("id")) {
                    if (ids.length() > 0) ids.append(",");
                    ids.append(row.get("id").asText());
                }
            }
            if (ids.length() > 0) {
                logger.info("Deleting " + targetDataNode.size() + " existing rows from target TDP " + targetTdpId);
                makeHttpRequestWithBody(
                        "https://app.testsigma.com/api/v1/test_data_sets/bulk?ids=" + ids,
                        "DELETE", "{}", apiKey, logger);
            }
        }

        JsonNode dataNode = referenceJson.get("data");
        if (dataNode == null || !dataNode.isArray()) {
            throw new RuntimeException("No data array found in reference TDP response");
        }
        logger.info("Reference TDP has " + dataNode.size() + " rows — sending as full replacement to target");

        // Strip id and testDataProfileId so the server inserts fresh rows under the target.
        ArrayNode cleanData = objectMapper.createArrayNode();
        for (JsonNode row : dataNode) {
            ObjectNode cleanRow = row.deepCopy();
            cleanRow.remove("id");
            cleanRow.remove("testDataProfileId");
            cleanData.add(cleanRow);
        }

        // Keep the target's own name — using the reference name causes a unique-constraint
        // violation when both TDPs share the same version.
        String targetName = targetJson.has("testDataName")
                ? targetJson.get("testDataName").asText()
                : referenceJson.get("testDataName").asText();

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("testDataName", targetName);
        if (referenceJson.has("columns")) {
            requestBody.set("columns", referenceJson.get("columns"));
        }
        requestBody.set("data", cleanData);

        return makeHttpRequestWithBody(baseUrl + targetTdpId, "PUT", objectMapper.writeValueAsString(requestBody), apiKey, logger);
    }

    /**
     * Adds a new column (parameter) to an existing TDP.
     * Fetches the current TDP, adds the new column with the given default value
     * to every existing row, and PUTs the full data back.
     *
     * @param tdpId        The TDP ID
     * @param columnName   The name of the new column/parameter
     * @param defaultValue The default value to set for all existing rows
     * @param apiKey       The Bearer token for authentication
     * @param logger       Logger instance
     * @return The API response as a string
     * @throws Exception if the request or parsing fails
     */
    public static String addTDPColumn(String tdpId, String columnName, String defaultValue,
                                       String apiKey, Logger logger) throws Exception {
        String url = "https://app.testsigma.com/api/v1/test_data/" + tdpId;
        logger.info("Adding new column '" + columnName + "' with default value '" + defaultValue + "' to TDP " + tdpId);

        String existingResponse = makeHttpRequest2(url, apiKey, logger);
        JsonNode existing = objectMapper.readTree(existingResponse);

        JsonNode existingDataNode = existing.get("data");
        if (existingDataNode == null || !existingDataNode.isArray()) {
            throw new RuntimeException("No data array found in existing TDP response");
        }
        ArrayNode existingData = (ArrayNode) existingDataNode;

        for (JsonNode row : existingData) {
            JsonNode rowDataNode = row.get("data");
            if (rowDataNode != null && rowDataNode.isObject()) {
                ((ObjectNode) rowDataNode).put(columnName, defaultValue);
            }
        }

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("testDataName", existing.get("testDataName").asText());
        requestBody.set("data", existingData);

        return makeHttpRequestWithBody(url, "PUT", objectMapper.writeValueAsString(requestBody), apiKey, logger);
    }

}
