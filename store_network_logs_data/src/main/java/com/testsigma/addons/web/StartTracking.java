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
import org.openqa.selenium.devtools.v124.network.Network;
import org.openqa.selenium.devtools.v124.network.model.RequestId;
import org.openqa.selenium.remote.Augmenter;

import java.util.Optional;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.saveAllNetworkData;

@Data
@Action(actionText = "Add Network Listener to find the response for the request url url_value" +
        " and the request method method_value",
        description = "Add Network Listener to find the response for the given request url and given request method",
        applicationType = ApplicationType.WEB)
public class StartTracking extends WebAction {

    @TestData(reference = "url_value")
    private com.testsigma.sdk.TestData urlValue;

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
            DevTools devTool = ((HasDevTools) driver).getDevTools();
            devTool.createSessionIfThereIsNotOne();
            logger.info("DevTools session successfully created.");

            // Enable network interception with high buffer size
            logger.info("Enabling network interception...");
            devTool.send(Network.enable(Optional.empty(),
                    Optional.empty(), Optional.of(100000000)));

            final RequestId[] requestIds = new RequestId[1];
            final String[] capturedRequestHeaders = new String[1];

            // Listener to intercept network requests
            logger.info("Adding listener for network requests...");
            devTool.addListener(Network.requestWillBeSent(), request -> {
                String requestUrl = request.getRequest().getUrl();

                request.getRequest().getHeaders();
                logger.info("Intercepted request URL: " + requestUrl);

                // Check if the URL and method match the specified criteria
                if (requestUrl.contains(urlValue.getValue().toString()) &&
                        request.getRequest().getMethod().equalsIgnoreCase(methodValue.getValue().toString())) {
                    logger.info("Matching request found with URL: " + requestUrl +
                            " and method: " + methodValue.getValue());
                    requestIds[0] = request.getRequestId();
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
                if (response.getResponse().getUrl().contains(urlValue.getValue().toString()) &&
                        requestIds[0] != null && requestIds[0].toString().equals(response.getRequestId().toString())) {
                    logger.info("Matching response found for URL: " + response.getResponse().getUrl());
                    try {
                        // Retrieve the response body using the captured RequestId
                        String responseBody = devTool.send(Network.getResponseBody(requestIds[0])).getBody();
                        int status = response.getResponse().getStatus();
                        response.getResponse().getHeaders().forEach((key, value) -> {
                            logger.info("Response Header - " + key + ": " + value);
                        });
                        logger.info("Response status: " + status);
                        logger.info("Storing all network data together...");
                        String requestHeaders = capturedRequestHeaders[0] != null ? capturedRequestHeaders[0] : "";
                        logger.info("Request headers to store: " + requestHeaders);
                        logger.info("Response body to store: " + responseBody);

                        // Store all data together in one operation
                        JsonObject allData = new JsonObject();
                        allData.addProperty("statusCode", status);

                        JsonArray headersArray = new JsonArray();
                        headersArray.add(requestHeaders);
                        allData.add("requestHeaders", headersArray);

                        JsonArray responseArray = new JsonArray();
                        responseArray.add(responseBody);
                        allData.add("responseBody", responseArray);

                        // Save all data at once
                        saveAllNetworkData(testCaseResult.getId(), allData, logger);
                        logger.info("All network data stored together successfully.");
                    } catch (Exception e) {
                        logger.warn("Error while storing response body: " + ExceptionUtils.getStackTrace(e));
                    }
                }
            });

        } catch (Exception e) {
            // Log the exception details and set the error message
            logger.warn("Exception occurred during execution: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while adding Network Response Listener" +
                    " to the driver: " + e.getMessage());
            return Result.FAILED;
        }

        // Log success message
        logger.info("Successfully added Network Response Listener to the driver.");
        setSuccessMessage("Added Network Response Listener to the driver.");
        return Result.SUCCESS;
    }
}
