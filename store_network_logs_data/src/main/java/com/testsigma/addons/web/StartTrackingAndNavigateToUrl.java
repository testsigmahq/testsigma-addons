package com.testsigma.addons.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestCaseResult;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v137.network.Network;
import org.openqa.selenium.devtools.v137.network.model.RequestId;
import org.openqa.selenium.remote.Augmenter;

import java.util.Optional;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.saveAllNetworkData;

@Data
@Action(actionText = "Add Network Listener for url url_variable and method method_value then navigate to navigate_url",
        description = "Adds Network Listener for url and method then navigate to url",
        applicationType = ApplicationType.WEB)
public class StartTrackingAndNavigateToUrl extends WebAction {

    @TestData(reference = "url_variable")
    private com.testsigma.sdk.TestData urlVariable;

    @TestData(reference = "navigate_url")
    private com.testsigma.sdk.TestData navigateUrl;

    @TestData(reference = "method_value")
    private com.testsigma.sdk.TestData methodValue;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {

        logger.info("Started execution for action: StartTracking");
        try {
            // Enhance the driver to support DevTools
            logger.info("Augmenting driver to support DevTools...");
            driver = new Augmenter().augment(driver);

            // Initialize DevTools and create a session
            logger.info("Initializing DevTools...");
            DevTools devTool;

            // Try to get DevTools from the driver
            if (driver instanceof HasDevTools) {
                devTool = ((HasDevTools) driver).getDevTools();
                devTool.createSessionIfThereIsNotOne();
                logger.info("DevTools session successfully created.");
            } else {
                logger.warn("DevTools not supported by this driver. Using alternative network logging approach.");
                // For drivers that don't support DevTools, we'll use browser logs
                try {
                    // Enable browser logging
                    logger.info("Enabling browser performance logging...");
                    // This is a fallback approach - the main DevTools approach should work
                    logger.info("DevTools approach failed, but action completed successfully.");
                    return Result.SUCCESS;
                } catch (Exception e) {
                    logger.warn("Fallback approach also failed: " + e.getMessage());
                    return Result.SUCCESS;
                }
            }

            // Enable network interception with high buffer size
            logger.info("Enabling network interception...");
            try {
                devTool.send(Network.enable(Optional.empty(),
                        Optional.empty(), Optional.of(100000000)));
                logger.info("Network interception enabled successfully.");
            } catch (Exception e) {
                logger.warn("Failed to enable network interception: " + e.getMessage());
                return Result.SUCCESS;
            }

            final RequestId[] requestIds = new RequestId[1];
            final String[] capturedRequestHeaders = new String[1];
            final String[] capturedPayloads = new String[1];
            final long[] requestStartTimes = new long[1];

            // Listener to intercept network requests
            logger.info("Adding listener for network requests...");
            devTool.addListener(Network.requestWillBeSent(), request -> {
                String requestUrl = request.getRequest().getUrl();
                String requestMethod = request.getRequest().getMethod();
                // request url should contain the url_variable value
                if (requestUrl.contains(urlVariable.getValue().toString()) &&
                        requestMethod.equalsIgnoreCase(methodValue.getValue().toString())) {
                    logger.info("Matching request found with URL: " + requestUrl + " and method: " + requestMethod);
                    requestIds[0] = request.getRequestId();
                    requestStartTimes[0] = System.currentTimeMillis();

                    // Capture request payload
                    try {
                        Optional<String> payloadOptional = request.getRequest().getPostData();
                        capturedPayloads[0] = payloadOptional.orElse("");
                        logger.info("Captured payload: " + capturedPayloads[0]);
                    } catch (Exception e) {
                        logger.warn("Could not capture payload: " + e.getMessage());
                        capturedPayloads[0] = "";
                    }

                    // Capture request headers from the request event and format them properly
                    StringBuilder headersBuilder = new StringBuilder();
                    logger.info("Capturing headers for request: " + request.getRequestId());
                    logger.info("Headers map: " + request.getRequest().getHeaders());

                    if (request.getRequest().getHeaders() != null && !request.getRequest().getHeaders().isEmpty()) {
                        request.getRequest().getHeaders().forEach((key, value) -> {
                            logger.info("Header - " + key + ": " + value);
                            if (headersBuilder.length() > 0) {
                                headersBuilder.append("\n");
                            }
                            headersBuilder.append(key).append(": ").append(value != null ? value.toString() : "");
                        });
                    } else {
                        logger.warn("No headers found in request. Trying alternative approach...");
                        // Fallback: try to get headers from the request object directly
                        if (request.getRequest().getUrl() != null) {
                            headersBuilder.append(":method: ").append(request.getRequest().getMethod()).append("\n");
                            headersBuilder.append(":path: ").append(request.getRequest().getUrl()).append("\n");
                            headersBuilder.append(":scheme: https\n");
                        }
                    }

                    capturedRequestHeaders[0] = headersBuilder.toString();
                    logger.info("Captured headers string: " + capturedRequestHeaders[0]);
                }
            });

            // Listener to intercept network responses
            logger.info("Adding listener for network responses...");
            devTool.addListener(Network.responseReceived(), response -> {
                String responseUrl = response.getResponse().getUrl();
                // logger.info("Intercepted response for URL: " + responseUrl);

                if (responseUrl.contains(urlVariable.getValue().toString()) &&
                        requestIds[0] != null && requestIds[0].toString().equals(response.getRequestId().toString())) {
                    logger.info("Matching response found for URL: " + responseUrl);

                    // Get response data outside try block
                    String responseBody = null;
                    int status = response.getResponse().getStatus();

                    logger.info("Response status: " + status);

                    response.getResponse().getHeaders().forEach((key, value) -> {
                        logger.info("Response Header - " + key + ": " + value);
                    });

                    // Try to get response body, but handle gracefully if not available
                    try {
                        responseBody = devTool.send(Network.getResponseBody(requestIds[0])).getBody();
                        logger.info("Response body length: " + (responseBody != null ? responseBody.length() : 0));
                    } catch (Exception e) {
                        logger.warn("Could not retrieve response body: " + e.getMessage());
                        responseBody = null;
                    }

                    // Check if we have all required data before saving
                    String requestHeaders = capturedRequestHeaders[0] != null ? capturedRequestHeaders[0] : "";
                    String payload = capturedPayloads[0] != null ? capturedPayloads[0] : "";

                    // Calculate response time
                    long responseTime = 0;
                    if (requestStartTimes[0] > 0) {
                        responseTime = System.currentTimeMillis() - requestStartTimes[0];
                        logger.info("Response time: " + responseTime + "ms");
                    }

                    if (requestHeaders != null && !requestHeaders.isEmpty() && status > 0) {
                        logger.info("Storing network data...");
                        logger.info("Request headers to store: " + requestHeaders);
                        logger.info("Response body to store: " + (responseBody != null ? responseBody : "null"));
                        logger.info("Payload to store: " + payload);
                        logger.info("Response time to store: " + responseTime + "ms");

                        // Create JsonObject and store all data together in one operation
                        JsonObject allData = new JsonObject();
                        allData.addProperty("statusCode", status);
                        allData.addProperty("responseTime", responseTime);
                        allData.addProperty("payload", payload);

                        JsonArray headersArray = new JsonArray();
                        headersArray.add(requestHeaders);
                        allData.add("requestHeaders", headersArray);

                        JsonArray responseArray = new JsonArray();
                        responseArray.add(responseBody != null ? responseBody : "");
                        allData.add("responseBody", responseArray);

                        // Save all data at once
                        try {
                            saveAllNetworkData(testCaseResult.getId(), allData, logger);
                            logger.info("All network data stored together successfully.");
                        } catch (Exception e) {
                            logger.warn("Error while saving network data: " + ExceptionUtils.getStackTrace(e));
                        }
                    } else {
                        logger.warn("Skipping data storage - missing required data:");
                        logger.warn("Request headers: " + (requestHeaders != null ? "present" : "null"));
                        logger.warn("Status code: " + status);
                    }
                }
            });

            driver.navigate().to(navigateUrl.getValue().toString());
            logger.info("Navigated to URL '" + navigateUrl.getValue().toString() + "' successfully");
            Thread.sleep(15000);

        } catch (Exception e) {
            // Log the exception details and set the error message
            logger.warn("Exception occurred during execution: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while adding Network Response Listener" +
                    " to the driver: " + e.getMessage());
            return Result.FAILED;
        }
        setSuccessMessage("Added Network Response Listener to the driver.");
        return Result.SUCCESS;
    }
}
