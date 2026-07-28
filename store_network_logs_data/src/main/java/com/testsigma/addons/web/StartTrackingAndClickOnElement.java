package com.testsigma.addons.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.testsigma.addons.web.utilities.RawCdpNetworkSession;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestCaseResult;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.saveAllNetworkData;
import static com.testsigma.addons.web.utilities.ResponseDataUtilities.savePayloadData;

@Data
@Action(actionText = "Add Network Listener for url url_variable and method method_value then perform element click element-locator",
        description = "Adds Network Listener for url and method then perform element click action",
        applicationType = ApplicationType.WEB)
public class StartTrackingAndClickOnElement extends WebAction {

    @TestData(reference = "url_variable")
    private com.testsigma.sdk.TestData urlVariable;

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "method_value")
    private com.testsigma.sdk.TestData methodValue;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {
        logger.info("Started execution for action: StartTrackingAndClickOnElement");
        try {
            String urlPattern = urlVariable.getValue().toString();
            String method = methodValue.getValue().toString();
            Duration cdpTimeout = Duration.ofSeconds(10);
            logger.info("Tracking requests where url contains \"" + urlPattern + "\" and method = " + method);
            logger.info("driver class: " + driver.getClass().getName());

            RawCdpNetworkSession session = RawCdpNetworkSession.attach(driver, logger, cdpTimeout);

            // Only the first matching request is captured; subsequent matches are ignored.
            AtomicBoolean captured = new AtomicBoolean(false);
            String[] capturedRequestId = new String[1];
            String[] capturedRequestHeaders = new String[1];
            long[] requestStartTime = new long[1];

            session.onRequestWillBeSent((seq, params) -> {
                try {
                    if (captured.get()) return;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> request = (Map<String, Object>) params.get("request");
                    if (request == null) return;

                    String requestUrl = String.valueOf(request.get("url"));
                    String requestMethod = String.valueOf(request.get("method"));
                    if (!requestUrl.contains(urlPattern) || !requestMethod.equalsIgnoreCase(method)) {
                        return;
                    }
                    if (!captured.compareAndSet(false, true)) return;

                    String requestId = String.valueOf(params.get("requestId"));
                    capturedRequestId[0] = requestId;
                    requestStartTime[0] = System.currentTimeMillis();
                    capturedRequestHeaders[0] = formatHeaders(request.get("headers"), requestMethod, requestUrl);
                    logger.info("Matching request captured: " + requestUrl + " id: " + requestId);

                    Object postData = request.get("postData");
                    String payload = postData == null ? "" : postData.toString();
                    if (!payload.isEmpty()) {
                        savePayloadData(testCaseResult.getId(), payload, logger);
                    }
                } catch (Exception e) {
                    logger.warn("Error while handling Network.requestWillBeSent: " + ExceptionUtils.getStackTrace(e));
                }
            });

            session.onResponseReceived((seq, params) -> {
                try {
                    String requestId = String.valueOf(params.get("requestId"));
                    if (capturedRequestId[0] == null || !capturedRequestId[0].equals(requestId)) return;

                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) params.get("response");
                    if (response == null) return;

                    String responseUrl = String.valueOf(response.get("url"));
                    int status = ((Number) response.get("status")).intValue();
                    logger.info("Matching response for URL: " + responseUrl + " status: " + status);

                    String requestHeaders = capturedRequestHeaders[0] != null ? capturedRequestHeaders[0] : "";
                    long responseTime = requestStartTime[0] > 0 ? System.currentTimeMillis() - requestStartTime[0] : 0;
                    logger.info("Response time: " + responseTime + "ms");

                    if (requestHeaders.isEmpty() || status <= 0) {
                        logger.warn("Skipping storage - headers empty: " + requestHeaders.isEmpty() + ", status: " + status);
                        return;
                    }

                    Optional<String> responseBody = session.getResponseBody(requestId, cdpTimeout);
                    logger.info("Response body length: " + responseBody.map(String::length).orElse(0));

                    JsonObject allData = new JsonObject();
                    allData.addProperty("statusCode", status);
                    allData.addProperty("responseTime", responseTime);

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

            WebElement webElement = element.getElement();
            webElement.click();
            logger.info("Clicked on element successfully");
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
