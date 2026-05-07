package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Action(actionText = "Customised API Call to url api-endpoint with payload request-payload and auth token auth-token, " +
                "additional headers additional-headers, " +
                "filtering response with criteria filter-criteria and storing in runtime variable runtime-variable-for-response",
        description = "Makes a POST or GET API call with an optional bearer auth token, custom headers, and request payload. " +
                "auth-token adds 'Authorization: Bearer <token>'. " +
                "additional-headers accepts semicolon-separated name=value pairs (e.g. x-api-key=abc123;X-Portal-Key=mylab) " +
                "for APIs that require non-Bearer auth or extra headers. " +
                "filter-criteria accepts comma-separated field=value pairs (e.g. visible=false,uiType=Hidden). " +
                "Items matching ANY criterion are excluded from the JSON array response. " +
                "Stores the filtered response in the specified runtime variable.",
        applicationType = com.testsigma.sdk.ApplicationType.WEB)
public class CustomisedApiCall extends WebAction {

    @TestData(reference = "api-endpoint")
    private com.testsigma.sdk.TestData apiEndpoint;

    @TestData(reference = "request-payload")
    private com.testsigma.sdk.TestData requestPayload;

    @TestData(reference = "auth-token")
    private com.testsigma.sdk.TestData authToken;

    @TestData(reference = "additional-headers")
    private com.testsigma.sdk.TestData additionalHeaders;

    @TestData(reference = "filter-criteria")
    private com.testsigma.sdk.TestData filterCriteria;

    @TestData(reference = "runtime-variable-for-response")
    private com.testsigma.sdk.TestData runtimeVariableForResponse;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] ARRAY_KEYS = {"data", "items", "result", "records", "fields", "list"};

    @Override
    public Result execute() {
        logger.info("[CustomisedApiCall] ===== Action execution started =====");

        // ── 1. Read and sanitise all inputs ───────────────────────────────────
        String endpoint  = valueOrEmpty(apiEndpoint);
        String payload   = valueOrEmpty(requestPayload);
        String token     = valueOrEmpty(authToken);
        String extraHdrs = valueOrEmpty(additionalHeaders);
        String criteria  = valueOrEmpty(filterCriteria);
        String rtVarName = valueOrEmpty(runtimeVariableForResponse);

        logger.info("[CustomisedApiCall] Input — endpoint     : " + endpoint);
        logger.info("[CustomisedApiCall] Input — payload      : " + (payload.isEmpty() ? "(none — will use GET)" : payload));
        logger.info("[CustomisedApiCall] Input — auth-token   : " + (token.isEmpty() ? "(none)" : "*** provided ***"));
        logger.info("[CustomisedApiCall] Input — extra-headers: " + (extraHdrs.isEmpty() ? "(none)" : extraHdrs));
        logger.info("[CustomisedApiCall] Input — filter       : " + (criteria.isEmpty() ? "(none)" : criteria));
        logger.info("[CustomisedApiCall] Input — runtime-var  : " + rtVarName);

        // ── 2. Validate mandatory inputs ──────────────────────────────────────
        if (endpoint.isEmpty()) {
            setErrorMessage("'api-endpoint' is required but was empty or not provided. " +
                    "Please enter a valid URL in the api-endpoint test data field.");
            logger.warn("[CustomisedApiCall] Validation failed: api-endpoint is empty");
            return Result.FAILED;
        }

        if (rtVarName.isEmpty()) {
            setErrorMessage("'runtime-variable-for-response' is required but was empty or not provided. " +
                    "Please specify the runtime variable name where the response should be stored.");
            logger.warn("[CustomisedApiCall] Validation failed: runtime-variable-for-response is empty");
            return Result.FAILED;
        }

        // ── 3. Make the API call ──────────────────────────────────────────────
        String responseBody;
        try {
            responseBody = callApi(endpoint, payload, token, extraHdrs);
        } catch (IllegalArgumentException e) {
            setErrorMessage("Invalid api-endpoint URL '" + endpoint + "': " + e.getMessage() +
                    ". Check for typos, missing protocol (https://), or extra spaces.");
            logger.warn("[CustomisedApiCall] Invalid URL '" + endpoint + "': " + e.getMessage());
            return Result.FAILED;
        } catch (RuntimeException e) {
            // Non-2xx HTTP response — message already contains status + body
            setErrorMessage(e.getMessage());
            logger.warn("[CustomisedApiCall] API call failed: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Network error while calling '" + endpoint + "': " + e.getMessage() +
                    ". Verify the endpoint is reachable and the server is running.");
            logger.warn("[CustomisedApiCall] Network/IO error: " + e.getMessage());
            return Result.FAILED;
        }

        // ── 4. Apply filter criteria ──────────────────────────────────────────
        if (!criteria.isEmpty()) {
            try {
                responseBody = applyFilterCriteria(responseBody, criteria);
            } catch (Exception e) {
                setErrorMessage("Failed to parse or filter the API response. " +
                        "Verify the response is valid JSON and filter-criteria format is correct " +
                        "(e.g. visible=false,uiType=Hidden). Error: " + e.getMessage());
                logger.warn("[CustomisedApiCall] Filtering failed: " + e.getMessage());
                return Result.FAILED;
            }
        }

        // ── 5. Store result in runtime variable ───────────────────────────────
        try {
            runTimeData.setKey(rtVarName);
            runTimeData.setValue(responseBody);
            logger.info("[CustomisedApiCall] Response stored in runtime variable '" + rtVarName + "'");
        } catch (Exception e) {
            setErrorMessage("Failed to store response in runtime variable '" + rtVarName + "': " + e.getMessage());
            logger.warn("[CustomisedApiCall] Could not set runtime variable '" + rtVarName + "': " + e.getMessage());
            return Result.FAILED;
        }

        logger.info("[CustomisedApiCall] ===== Action execution completed successfully =====");
        return Result.SUCCESS;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP call
    // ─────────────────────────────────────────────────────────────────────────

    private String callApi(String endpoint, String payload, String token, String extraHeaders) throws Exception {
        String method = payload.isEmpty() ? "GET" : "POST";
        logger.info("[CustomisedApiCall] Preparing " + method + " request to: " + endpoint);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        logger.info("[CustomisedApiCall] Default headers set: Content-Type=application/json, Accept=application/json");

        if (!token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
            logger.info("[CustomisedApiCall] Added header: Authorization=Bearer ***");
        }

        if (!extraHeaders.isEmpty()) {
            logger.info("[CustomisedApiCall] Parsing additional-headers: '" + extraHeaders + "'");
            for (String entry : extraHeaders.split(";")) {
                String trimmedEntry = entry.trim();
                if (trimmedEntry.isEmpty()) {
                    logger.warn("[CustomisedApiCall] Skipping empty header entry (extra semicolon?)");
                    continue;
                }
                String[] parts = trimmedEntry.split("=", 2);
                if (parts.length == 2 && !parts[0].trim().isEmpty()) {
                    String headerName  = parts[0].trim();
                    String headerValue = parts[1].trim();
                    builder.header(headerName, headerValue);
                    boolean isSensitive = headerName.toLowerCase().contains("key")
                            || headerName.toLowerCase().contains("token");
                    logger.info("[CustomisedApiCall] Added header: " + headerName + "=" + (isSensitive ? "***" : headerValue));
                } else {
                    logger.warn("[CustomisedApiCall] Skipping malformed header entry '" + trimmedEntry
                            + "' — expected format: name=value");
                }
            }
        }

        if (!payload.isEmpty()) {
            builder.POST(HttpRequest.BodyPublishers.ofString(payload));
            logger.info("[CustomisedApiCall] Request body set (" + payload.length() + " chars)");
        } else {
            builder.GET();
            logger.info("[CustomisedApiCall] No payload provided — using GET");
        }

        logger.info("[CustomisedApiCall] Sending request...");
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        int responseLength = response.body() != null ? response.body().length() : 0;
        logger.info("[CustomisedApiCall] Response received — HTTP " + status + ", body length: " + responseLength + " chars");

        if (status < 200 || status >= 300) {
            logger.warn("[CustomisedApiCall] Non-2xx response. Status: " + status + ", Body: " + response.body());
            throw new RuntimeException("API call to '" + endpoint + "' failed with HTTP " + status +
                    ". Response body: " + response.body() +
                    ". Check the endpoint URL, request payload, and authentication headers.");
        }

        logger.info("[CustomisedApiCall] API call succeeded with HTTP " + status);
        return response.body();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filtering
    // ─────────────────────────────────────────────────────────────────────────

    private String applyFilterCriteria(String responseBody, String criteria) throws Exception {
        logger.info("[CustomisedApiCall] Applying filter criteria: '" + criteria + "'");
        List<String[]> filters = parseCriteria(criteria);

        if (filters.isEmpty()) {
            logger.warn("[CustomisedApiCall] No valid filter rules parsed from criteria '" + criteria
                    + "' — response returned unfiltered");
            return responseBody;
        }

        JsonNode root;
        try {
            root = MAPPER.readTree(responseBody);
        } catch (Exception e) {
            throw new Exception("Response is not valid JSON. Cannot apply filter. " +
                    "Raw response (first 200 chars): " +
                    responseBody.substring(0, Math.min(200, responseBody.length())));
        }

        if (root.isArray()) {
            int before = root.size();
            ArrayNode filtered = filterArray((ArrayNode) root, filters);
            logger.info("[CustomisedApiCall] Filtered top-level array: " + before + " items → " + filtered.size() + " items kept");
            return MAPPER.writeValueAsString(filtered);
        }

        if (root.isObject()) {
            for (String key : ARRAY_KEYS) {
                if (root.has(key) && root.get(key).isArray()) {
                    ArrayNode source = (ArrayNode) root.get(key);
                    int before = source.size();
                    ArrayNode filtered = filterArray(source, filters);
                    ((ObjectNode) root).set(key, filtered);
                    logger.info("[CustomisedApiCall] Filtered '" + key + "' array: " + before
                            + " items → " + filtered.size() + " items kept");
                    return MAPPER.writeValueAsString(root);
                }
            }
            logger.warn("[CustomisedApiCall] Response is a JSON object but no array found under known keys "
                    + Arrays.toString(ARRAY_KEYS) + ". Filter not applied — response returned as-is.");
        }

        return responseBody;
    }

    private List<String[]> parseCriteria(String criteria) {
        List<String[]> filters = new ArrayList<>();
        for (String criterion : criteria.split(",")) {
            String trimmed = criterion.trim();
            if (trimmed.isEmpty()) {
                logger.warn("[CustomisedApiCall] Skipping empty filter entry (extra comma?)");
                continue;
            }
            String[] parts = trimmed.split("=", 2);
            if (parts.length == 2 && !parts[0].trim().isEmpty()) {
                String field = parts[0].trim();
                String value = parts[1].trim();
                filters.add(new String[]{field, value});
                logger.info("[CustomisedApiCall] Filter rule added: exclude items where '" + field + "' = '" + value + "'");
            } else {
                logger.warn("[CustomisedApiCall] Skipping malformed filter entry '" + trimmed
                        + "' — expected format: field=value");
            }
        }
        return filters;
    }

    private ArrayNode filterArray(ArrayNode source, List<String[]> filters) {
        ArrayNode result = MAPPER.createArrayNode();
        for (JsonNode item : source) {
            if (matchesAnyFilter(item, filters)) {
                logger.info("[CustomisedApiCall] Item excluded — matched a filter rule");
            } else {
                result.add(item);
            }
        }
        return result;
    }

    private boolean matchesAnyFilter(JsonNode item, List<String[]> filters) {
        for (String[] filter : filters) {
            String field         = filter[0];
            String expectedValue = filter[1];

            if (!item.has(field)) {
                continue;
            }

            JsonNode node = item.get(field);
            // For text nodes use asText() to avoid JSON quotes; for booleans/numbers use toString()
            String actualValue = node.isTextual() ? node.asText() : node.toString();

            if (actualValue.trim().equalsIgnoreCase(expectedValue)) {
                logger.info("[CustomisedApiCall] Item excluded: field '" + field + "' matched value '" + expectedValue + "'");
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String valueOrEmpty(com.testsigma.sdk.TestData td) {
        if (td == null || td.getValue() == null) return "";
        return td.getValue().toString().trim();
    }
}
