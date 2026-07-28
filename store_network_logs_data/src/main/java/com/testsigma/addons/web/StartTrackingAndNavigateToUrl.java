package com.testsigma.addons.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.testsigma.addons.web.utilities.RawCdpNetworkSession;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestCaseResult;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
        logger.info("Started execution for action: StartTrackingAndNavigateToUrl");
        try {
            String urlPattern = urlVariable.getValue().toString();
            String method = methodValue.getValue().toString();
            Duration cdpTimeout = Duration.ofSeconds(10);
            logger.info("Tracking requests where url contains \"" + urlPattern + "\" and method = " + method);
            logger.info("driver class: " + driver.getClass().getName());

            RawCdpNetworkSession session = RawCdpNetworkSession.attach(driver, logger, cdpTimeout);

            // requestId -> {payload, requestHeaders string, start time} for requests matching the filter, until their response arrives
            Map<String, Object[]> pendingMatches = new ConcurrentHashMap<>();

            session.onRequestWillBeSent((seq, params) -> {
                try {
                    String requestId = String.valueOf(params.get("requestId"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> request = (Map<String, Object>) params.get("request");
                    if (request == null) return;

                    String requestUrl = String.valueOf(request.get("url"));
                    String requestMethod = String.valueOf(request.get("method"));
                    if (!requestUrl.contains(urlPattern) || !requestMethod.equalsIgnoreCase(method)) {
                        return;
                    }

                    logger.info("Matching request found with URL: " + requestUrl + " and method: " + requestMethod);
                    Object postData = request.get("postData");
                    String payload = postData == null ? "" : postData.toString();
                    String requestHeaders = formatHeaders(request.get("headers"), requestMethod, requestUrl);
                    pendingMatches.put(requestId, new Object[]{payload, requestHeaders, System.currentTimeMillis()});
                } catch (Exception e) {
                    logger.warn("Error while handling Network.requestWillBeSent: " + ExceptionUtils.getStackTrace(e));
                }
            });

            session.onResponseReceived((seq, params) -> {
                try {
                    String requestId = String.valueOf(params.get("requestId"));
                    Object[] pending = pendingMatches.remove(requestId);
                    if (pending == null) return;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) params.get("response");
                    if (response == null) return;

                    String responseUrl = String.valueOf(response.get("url"));
                    int status = ((Number) response.get("status")).intValue();
                    logger.info("Matching response found for URL: " + responseUrl + " status: " + status);

                    String payload = (String) pending[0];
                    String requestHeaders = (String) pending[1];
                    long startTime = (Long) pending[2];
                    long responseTime = System.currentTimeMillis() - startTime;

                    Optional<String> responseBody = session.getResponseBody(requestId, cdpTimeout);
                    logger.info("Response time: " + responseTime + "ms, body length: "
                            + responseBody.map(String::length).orElse(0));

                    if (requestHeaders.isEmpty() || status <= 0) {
                        logger.warn("Skipping data storage - missing required data. Request headers present: "
                                + !requestHeaders.isEmpty() + ", status: " + status);
                        return;
                    }

                    JsonObject allData = new JsonObject();
                    allData.addProperty("statusCode", status);
                    allData.addProperty("responseTime", responseTime);

                    JsonArray payloadArray = new JsonArray();
                    payloadArray.add(payload);
                    allData.add("payload", payloadArray);

                    JsonArray headersArray = new JsonArray();
                    headersArray.add(requestHeaders);
                    allData.add("requestHeaders", headersArray);

                    JsonArray responseArray = new JsonArray();
                    responseArray.add(responseBody.orElse(""));
                    allData.add("responseBody", responseArray);

                    saveAllNetworkData(testCaseResult.getId(), allData, logger);
                    logger.info("Network data stored successfully.");
                } catch (Exception e) {
                    logger.warn("Error while handling Network.responseReceived: " + ExceptionUtils.getStackTrace(e));
                }
            });

            logger.info("Raw CDP network listener attached.");

            driver.navigate().to(navigateUrl.getValue().toString());
            logger.info("Navigated to URL '" + navigateUrl.getValue().toString() + "' successfully");
            Thread.sleep(15000);

        } catch (Exception e) {
            logger.warn("Exception occurred during execution: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while adding Network Response Listener" +
                    " to the driver: " + e.getMessage());
            return Result.FAILED;
        }
        setSuccessMessage("Added Network Response Listener to the driver.");
        return Result.SUCCESS;
    }

    @SuppressWarnings("unchecked")
    private String formatHeaders(Object headersObj, String requestMethod, String requestUrl) {
        StringBuilder headersBuilder = new StringBuilder();
        if (headersObj instanceof Map) {
            ((Map<String, Object>) headersObj).forEach((key, value) -> {
                if (headersBuilder.length() > 0) headersBuilder.append("\n");
                headersBuilder.append(key).append(": ").append(value != null ? value.toString() : "");
            });
        }
        if (headersBuilder.length() == 0) {
            headersBuilder.append(":method: ").append(requestMethod).append("\n");
            headersBuilder.append(":path: ").append(requestUrl).append("\n");
            headersBuilder.append(":scheme: https\n");
        }
        return headersBuilder.toString();
    }
}
