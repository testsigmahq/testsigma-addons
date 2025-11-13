package com.testsigma.addons.hooks;

 import com.testsigma.sdk.Hook;
import com.testsigma.sdk.HookType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestPlanHook;


@TestPlanHook(name = "Make API Call using bearer token", type = HookType.AFTER)
public class GenericApiAction extends Hook {

    @TestData(reference = "{api_url}")
    private com.testsigma.sdk.TestData apiUrl;

    @TestData(reference = "{http_method}")
    private com.testsigma.sdk.TestData httpMethod;

    @TestData(reference = "{query_params}")
    private com.testsigma.sdk.TestData queryParams;

    @TestData(reference = "{request_body}")
    private com.testsigma.sdk.TestData requestBody;

    @TestData(reference = "{authorization_token}")
    private com.testsigma.sdk.TestData authorizationToken;

    @TestData(reference = "{headers}")
    private com.testsigma.sdk.TestData headers;

    @Override
    protected Result execute() {
        try {
            logger.info("Starting Generic API Call Hook execution.");

            // Safely extract values with null handling
            String apiUri = getValueSafely(apiUrl);
            String method = getValueSafely(httpMethod);
            String query = getValueSafely(queryParams);
            String requestBodyStr = getValueSafely(requestBody);
            String authToken = getValueSafely(authorizationToken);
            String customHeaders = getValueSafely(headers);

            // Validate required fields - only apiUrl and httpMethod are required
            if (apiUri == null || apiUri.isEmpty()) {
                String errorMsg = "API URL is required but was not provided.";
                logger.info(errorMsg);
                setErrorMessage(errorMsg);
                return Result.FAILURE;
            }

            if (method == null || method.isEmpty()) {
                String errorMsg = "HTTP method is required but was not provided.";
                logger.info(errorMsg);
                setErrorMessage(errorMsg);
                return Result.FAILURE;
            }

            // Normalize method to uppercase (already trimmed by getValueSafely)
            method = method.toUpperCase();

            HttpApiCallUtil apiUtil = new HttpApiCallUtil(logger);
            logger.info(String.format("Making API call with available details - apiUri: %s, method: %s, query: %s, requestBody: %s, authToken: %s, customHeaders: %s",
                    apiUri, method, 
                    query != null ? "provided" : "null", 
                    requestBodyStr != null ? "provided" : "null",
                    authToken != null ? "provided" : "null",
                    customHeaders != null ? "provided" : "null"));

            // call to api endpoint with available details
            String response = apiUtil.makeApiCall(apiUri, method, query, requestBodyStr, authToken, customHeaders);
            logger.info("API call successful. Response: " + response);
            setSuccessMessage("api Response: " + response);
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.info("API call failed: " + e.getMessage());
            setErrorMessage("Failed to make API call: " + e.getMessage());
            return Result.FAILURE;
        }
    }

    /**
     * Safely extracts value from TestData object, handling all null cases
     * @param testData The TestData object to extract value from
     * @return The trimmed string value, or null if testData or its value is null/empty
     */
    private String getValueSafely(com.testsigma.sdk.TestData testData) {
        if (testData == null) {
            return null;
        }
        try {
            Object value = testData.getValue();
            if (value == null) {
                return null;
            }
            String strValue = value.toString().trim();
            return strValue.isEmpty() ? null : strValue;
        } catch (Exception e) {
            logger.info("Error extracting value from TestData: " + e.getMessage());
            return null;
        }
    }
}

