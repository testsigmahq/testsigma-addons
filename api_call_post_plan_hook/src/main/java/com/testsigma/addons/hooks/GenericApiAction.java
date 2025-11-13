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

            String apiUri = apiUrl.getValue().toString();
            String method = httpMethod.getValue().toString().toUpperCase();
            String query = (queryParams != null) ? queryParams.getValue().toString() : null;
            String requestBodyStr = (requestBody != null) ? requestBody.getValue().toString() : null;
            String authToken = (authorizationToken != null) ? authorizationToken.getValue().toString() : null;
            String customHeaders = (headers != null) ? headers.getValue().toString() : null;
            HttpApiCallUtil apiUtil = new HttpApiCallUtil(logger);
            logger.info(String.format("received apiUri: %s, method: %s, query: %s, requestBody: %s, customHeaders: %s",
                    apiUri, method, query, requestBodyStr, customHeaders));

            // call to api endpoint
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
}

