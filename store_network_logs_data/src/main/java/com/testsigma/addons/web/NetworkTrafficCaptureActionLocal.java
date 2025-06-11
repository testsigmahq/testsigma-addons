package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.util.Iterator;

@Data
@Action(
        actionText = "Store value for key keyToExtract in network urlPattern for url targetUrl in variable variableName",
        description = "Captures network urls and stores specified key value from response in a variable for local execution",
        applicationType = ApplicationType.WEB
)
public class NetworkTrafficCaptureActionLocal extends WebAction {

    @TestData(reference = "keyToExtract")
    private com.testsigma.sdk.TestData keyToExtract;

    @TestData(reference = "urlPattern")
    private com.testsigma.sdk.TestData urlPattern;

    @TestData(reference = "targetUrl")
    private com.testsigma.sdk.TestData targetUrl;

    @TestData(reference = "variableName")
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private BrowserMobProxy proxy;
    private WebDriver proxyDriver;

    @Override
    public Result execute() {
        try {
            // Logging start of execution
            logger.info("Starting network traffic capture");

            setupProxy();

            // Capturing network traffic based on user inputs
            String capturedValue = captureNetworkTraffic(
                    targetUrl.getValue().toString(),
                    urlPattern.getValue().toString(),
                    keyToExtract.getValue().toString()
            );

            // Storing the captured value in runtime data
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(capturedValue);

            // Success message with captured value
            setSuccessMessage("Successfully captured value: " + capturedValue +
                    " for key: " + keyToExtract.getValue() +
                    " and stored in variable: " + variableName.getValue());
            logger.info("Captured value successfully: " + capturedValue);

            return Result.SUCCESS;

        } catch (Exception e) {
            // Logging error if any exception occurs
            String errorMsg = "Error during network capture: " + e.getMessage();
            logger.warn("Error:" + ExceptionUtils.getStackTrace(e));
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
            return Result.FAILED;
        } finally {
            // Cleanup the resources after execution
            cleanup();
        }
    }

    private void setupProxy() {
        // Initialize and configure proxy
        logger.info("Setting up proxy...");
        proxy = new BrowserMobProxyServer();
        proxy.start(0);

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        logger.info("Selenium Proxy initialized: " + seleniumProxy);

        // Configure ChromeDriver with proxy settings
        ChromeOptions options = new ChromeOptions();
        options.setCapability(CapabilityType.PROXY, seleniumProxy);
        options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        proxyDriver = new ChromeDriver(options);
        logger.info("ChromeDriver initialized with proxy");

        // Enable HAR capture for both request and response content
        proxy.enableHarCaptureTypes(
                CaptureType.REQUEST_CONTENT,
                CaptureType.RESPONSE_CONTENT
        );
        logger.info("Proxy setup completed");
    }

    private String captureNetworkTraffic(String url, String urlPattern, String keyToExtract) throws Exception {
        // Start capturing traffic for the provided URL
        logger.info("Capturing network traffic for URL: " + url);
        proxy.newHar(url);
        proxyDriver.get(url);

        // Wait for network calls to complete
        Thread.sleep(5000); // Adjust based on network speed

        // Retrieve the captured HAR data
        Har har = proxy.getHar();
        StringBuilder capturedValues = new StringBuilder();

        // Loop through all captured requests and filter by URL pattern
        for (var entry : har.getLog().getEntries()) {
            String requestUrl = entry.getRequest().getUrl();
            if (requestUrl.contains(urlPattern)) {
                String responseBody = entry.getResponse().getContent().getText();
                logger.info("Response Body for URL: " + requestUrl);

                // Check for JSON response and extract the desired key value
                if (responseBody != null) {
                    if (responseBody.trim().startsWith("{")) {
                        // Parse JSON response
                        JSONObject jsonResponse = new JSONObject(new JSONTokener(responseBody));
                        logger.info("JSON Response: " + jsonResponse.toString());

                        String value = getNestedValue(jsonResponse, keyToExtract);
                        if (value != null) {
                            capturedValues.append(value).append(",");
                        } else {
                            logger.warn("Key " + keyToExtract + " not found in JSON response: " + jsonResponse.toString());
                        }
                    } else {
                        // Log non-JSON responses (binary data or plain text)
                        logger.warn("Non-JSON response detected, skipping: " + requestUrl);
                    }
                } else {
                    // Log if response is null
                    logger.warn("Response is null for URL: " + requestUrl);
                }
            }
        }

        // Check if any values were captured, otherwise throw an exception
        if (capturedValues.length() == 0) {
            throw new Exception("Key " + keyToExtract + " not found in responses matching pattern " + urlPattern);
        }

        // Remove trailing comma from captured values
        if (capturedValues.length() > 0 && capturedValues.charAt(capturedValues.length() - 1) == ',') {
            capturedValues.setLength(capturedValues.length() - 1);
        }

        logger.info("Captured values: " + capturedValues.toString());
        return capturedValues.toString();
    }

    private String getNestedValue(JSONObject jsonObject, String key) {
        // Recursive method to search for the key in nested JSON objects
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String currentKey = keys.next();
            if (currentKey.equals(key)) {
                return jsonObject.getString(currentKey);
            } else {
                try {
                    JSONObject nestedJson = jsonObject.getJSONObject(currentKey);
                    String value = getNestedValue(nestedJson, key);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    // Log and continue searching if nested object is found
                    logger.debug("Continuing search in nested JSON: " + currentKey);
                }
            }
        }
        return null;
    }

    private void cleanup() {
        // Cleaning up resources after execution
        logger.info("Cleaning up resources...");
        if (proxyDriver != null) {
            proxyDriver.quit();
            logger.info("ProxyDriver quit");
        }
        if (proxy != null) {
            proxy.stop();
            logger.info("Proxy stopped");
        }
    }
}
