package com.testsigma.addons.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.sdk.Logger;
import okhttp3.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class HttpApiCallUtil {
    Logger logger;

    private final OkHttpClient client = new OkHttpClient();
    private final MediaType mediaType = MediaType.parse("application/json");
    private final ObjectMapper mapper = new ObjectMapper();
    public HttpApiCallUtil(Logger logger) {
        this.logger = logger;
    }

    public String makeApiCall(String apiUri, String method, String queryParams, String requestBodyStr,
                              String authToken, String customHeaders) throws Exception {
        // Validate required parameters
        if (apiUri == null || apiUri.trim().isEmpty()) {
            throw new IllegalArgumentException("API URI cannot be null or empty");
        }
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be null or empty");
        }

        // Normalize method to uppercase
        method = method.trim().toUpperCase();
        apiUri = apiUri.trim();

        // Add query parameters if provided
        if (queryParams != null && !queryParams.trim().isEmpty()) {
            String queryString = buildQueryString(queryParams);
            if (!queryString.isEmpty()) {
                apiUri += (apiUri.contains("?") ? "&" : "?") + queryString;
            }
        }

        Request.Builder requestBuilder = new Request.Builder().url(apiUri);
        // Add headers
        addHeaders(requestBuilder, authToken, customHeaders);

        // Add request body for methods that support it
        RequestBody body = null;
        if (isMethodWithBody(method)) {
            body = buildRequestBody(requestBodyStr);
            requestBuilder.method(method, body);
        } else {
            requestBuilder.method(method, null);
        }

        Request request = requestBuilder.build();

        // Execute request
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            logger.info("Received response with code: " + response.code());
            if (!response.isSuccessful()) {
                throw new Exception("Unexpected response code: " + response.code() +
                        (responseBody.isEmpty() ? "" : ". Response: " + responseBody));
            }
            return responseBody;
        }
    }

    private String buildQueryString(String queryParamsStr) throws Exception {
        if (queryParamsStr == null) {
            return "";
        }
        String trimmed = queryParamsStr.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        // Try to parse as JSON first (key-value pairs)
        try {
            logger.info("Attempting to parse query parameters as JSON.");
            ObjectNode paramsJson = (ObjectNode) mapper.readTree(trimmed);
            StringBuilder queryBuilder = new StringBuilder();
            paramsJson.fields().forEachRemaining(entry -> {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                try {
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String value = URLEncoder.encode(entry.getValue().asText(), StandardCharsets.UTF_8);
                    queryBuilder.append(key).append("=").append(value);
                } catch (Exception e) {
                    // Ignore encoding errors
                }
            });
            logger.info("Constructed query string from JSON parameters: " + queryBuilder.toString());
            return queryBuilder.toString();
        } catch (Exception e) {
            // If not JSON, treat as URL-encoded query string
            logger.info("Warning: Could not parse query parameters as JSON: " + e.getMessage());
            logger.info("Using raw query parameters string.");
            return trimmed;
        }
    }

    private RequestBody buildRequestBody(String requestBody) throws Exception {
        if (requestBody == null) {
            // Return empty JSON body
            ObjectNode emptyBody = mapper.createObjectNode();
            String jsonBody = mapper.writeValueAsString(emptyBody);
            return RequestBody.create(jsonBody, mediaType);
        }

        // Try to parse as JSON to validate
        try {
            mapper.readTree(requestBody);
            // Valid JSON, use as is
            return RequestBody.create(requestBody, mediaType);
        } catch (Exception e) {
            // Not valid JSON, wrap in object or use as string
            ObjectNode bodyNode = mapper.createObjectNode();
            bodyNode.put("data", requestBody);
            String jsonBody = mapper.writeValueAsString(bodyNode);
            return RequestBody.create(jsonBody, mediaType);
        }
    }

    private void addHeaders(Request.Builder requestBuilder, String authorizationToken, String headers) throws Exception {
        // Add authorization header if provided
        if (authorizationToken != null) {
            requestBuilder.addHeader("authorization", "Bearer " + authorizationToken);
        }

        // Add default headers
        requestBuilder.addHeader("content-type", "application/json");
        requestBuilder.addHeader("accept", "application/json");

        // Add custom headers if provided
        if (headers != null) {
            try {
                ObjectNode headersJson = (ObjectNode) mapper.readTree(headers);
                headersJson.fields().forEachRemaining(entry -> {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue().asText());
                });
                logger.info("Added custom headers from JSON.");
            } catch (Exception e) {
                // If headers are not JSON, ignore
                logger.info("Warning: Could not parse headers as JSON: " + e.getMessage());
            }
        }
    }

    private boolean isMethodWithBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) ||
                "PATCH".equals(method) || "DELETE".equals(method);
    }

}
