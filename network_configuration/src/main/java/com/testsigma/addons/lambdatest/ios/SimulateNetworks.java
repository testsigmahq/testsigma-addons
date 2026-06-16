package com.testsigma.addons.lambdatest.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Data
@Action(actionText = "Simulate network to profile-name mode",
        description = "Simulates the behavior of given network.",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class SimulateNetworks extends IOSAction {

    @TestData(reference = "profile-name", allowedValues = {"no-network", "default", "2G", "3G", "4G"})
    com.testsigma.sdk.TestData profileName;

    @Override
    protected com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution to simulate network configuration on LambdaTest");

        final String successMessage = "Successfully switched to network-configuration : ";
        final String errorMessage = "Failed to switch to network configuration ";

        // source : https://www.lambdatest.com/support/docs/app-auto-network-throttling/

        try {
            logger.info("profile-name: " + profileName.getValue().toString());
            IOSDriver iosDriver = (IOSDriver) this.driver;
            String networkProfile = getLambdaTestProfileName(profileName.getValue().toString().toLowerCase());
            logger.info("updateNetworkProfile : " + networkProfile);
            iosDriver.executeScript("updateNetworkProfile=" + networkProfile);
            setSuccessMessage(successMessage + networkProfile);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.warn("stack trace: " + sw.toString());
            logger.info("profile-name: " + profileName.getValue().toString());

            IOSDriver iosDriver = (IOSDriver) this.driver;
            String sessionId = iosDriver.getSessionId().toString();
            String networkProfile = getLambdaTestProfileName(profileName.getValue().toString().toLowerCase());
            logger.info("updateNetworkProfile : " + networkProfile);

            // Get LambdaTest credentials from environment variables
            String username = "engineeringtestsigma";
            String accessKey = "LT_tlahXySA5pxQZSzKREDBbuzlzvm1sYEcI1ajsbDk8g0V2qS";

            logger.info("trying to update network profile via API");
            // Call LambdaTest API to update network profile
            try {
                updateNetworkViaAPI(sessionId, username, accessKey, networkProfile);
            } catch (Exception ex) {
                logger.warn("stack trace: " + ExceptionUtils.getStackTrace(ex));
                setErrorMessage(errorMessage + e.getMessage());
                result = com.testsigma.sdk.Result.FAILED;
                return result;
            }
            logger.info("Network profile updated successfully to " + profileName.getValue().toString());
            setSuccessMessage(successMessage + networkProfile);
        }

        return result;
    }

    private void updateNetworkViaAPI(String sessionId, String username, String accessKey, String networkProfile) throws Exception {
        String apiUrl = "https://mobile-api.lambdatest.com/mobile-automation/api/v1/sessions/" + sessionId + "/update_network";

        logger.info("Calling LambdaTest API: " + apiUrl);

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Set request method
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // Set Authorization header
            String auth = username + ":" + accessKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Prepare request body
            JSONObject requestBody = new JSONObject();

            // LambdaTest API accepts network profile names directly in the "mode" field
            // Supported values: "offline", "2g-gprs-good", "3g-umts-good", "4g-lte-good", "default", etc.
            requestBody.put("mode", networkProfile);

            String jsonInputString = requestBody.toString();
            logger.info("Request body: " + jsonInputString);

            // Enable output and send request
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get response code
            int responseCode = connection.getResponseCode();
            logger.info("Response code: " + responseCode);

            // Read response
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            logger.info("Response: " + response.toString());

            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception("API call failed with status code " + responseCode + ": " + response.toString());
            }

        } finally {
            connection.disconnect();
        }
    }

    private String getLambdaTestProfileName(String modifiedProfileName) {
        switch (modifiedProfileName) {
            case "no-network":
                return "offline";
            case "2g":
                return "2g-gprs-good";
            case "3g":
                return "3g-umts-good";
            case "4g":
                return "4g-lte-good";
            default:
                return "default";
        }
    }

}
