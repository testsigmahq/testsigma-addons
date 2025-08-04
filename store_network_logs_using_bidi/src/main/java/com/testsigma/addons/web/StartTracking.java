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
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.BytesValue;
import org.openqa.selenium.bidi.network.Header;

import java.util.ArrayList;
import java.util.List;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.saveAllNetworkData;

@Data
@Action(actionText = "bidi: Add Network Listener to find the request headers for the request url url_value ",
        description = "finds the request headers for the given request url",
        applicationType = ApplicationType.WEB)
public class StartTracking extends WebAction {

    @TestData(reference = "url_value")
    private com.testsigma.sdk.TestData urlValue;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {

        try {
            logger.info("Creating Chrome driver...");
            // The target URL we want to intercept
            String targetUrl = urlValue.getValue().toString();

            logger.info("Setting up Chrome options for WebSocket URL...");
            Network network = new Network(driver);
            logger.info("Initializing network monitoring...");

            // Set up network interception BEFORE navigating
            network.onBeforeRequestSent(beforeRequestSent -> {
                try {
                    String interceptedUrl = beforeRequestSent.getRequest().getUrl();
                    logger.info("Intercepted: " + interceptedUrl);
                    // Check if this is our target URL
                    if (interceptedUrl.contains(targetUrl)) {
                        logger.info("\n=== MATCHED TARGET URL ===");
                        logger.info("Full URL: " + interceptedUrl);
                        logger.info("\nRequest Headers:");
                        try {
                            // create a list of headers and add the header name and header value to the list
                            List<String> headerNameList = new ArrayList<>();
                            List<String> headerValueList = new ArrayList<>();
                            for (Header header : beforeRequestSent.getRequest().getHeaders()) {
                                String headerValue = header.getValue().getType() == BytesValue.Type.STRING
                                        ? header.getValue().getValue().toString()
                                        : new String(header.getValue().getValue());
                                logger.info(header.getName() + ": " + headerValue);
                                headerNameList.add(header.getName());
                                headerValueList.add(headerValue);
                            }
                            logger.info("Adding request body data using header names and values...");
                            // add request headers as json array
                            JsonObject requestHeaders = new JsonObject();
                            for (int i = 0; i < headerNameList.size(); i++) {
                                requestHeaders.addProperty(headerNameList.get(i), headerValueList.get(i));
                            }
                            JsonObject allData = new JsonObject();
                            JsonArray headersArray = new JsonArray();
                            headersArray.add(requestHeaders);
                            allData.add("requestHeaders", headersArray);

                            // Save all data at once
                            saveAllNetworkData(testCaseResult.getId(), allData, logger);
                        } catch (Exception headerError) {
                            logger.debug("Error reading headers: " + headerError.getMessage());
                        }
                        logger.info("=== END OF MATCHED REQUEST ===\n");
                    }
                } catch (Exception e) {
                    logger.debug("Error processing request: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.debug("Error in main execution: " + e.getMessage());
            setErrorMessage("Error in main execution: " + e.getMessage());
            return Result.FAILED;
        }
        logger.info("Successfully added Network Response Listener to the driver.");
        setSuccessMessage("Added Network Response Listener to the driver.");
        return Result.SUCCESS;
    }
}
