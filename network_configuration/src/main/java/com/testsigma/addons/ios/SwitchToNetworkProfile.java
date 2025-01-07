package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Data
@Action(actionText = "switch to network configuration profile-name",
        description = "switches the network type to no-network/3G/4G & default resets the initial network configuration",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class SwitchToNetworkProfile extends IOSAction {
    @TestData(reference = "profile-name", allowedValues = {"no-network", "default", "2G", "3G", "4G"})
    com.testsigma.sdk.TestData profileName;

    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution to simulate network configuration");

        final String successMessage = "Successfully switched to network-configuration : " + profileName.getValue().toString();
        final String errorMessage = "Failed to switch to network configuration";
        IOSDriver iosDriver = (IOSDriver) this.driver;
        String sessionId = iosDriver.getSessionId().toString().toLowerCase();

        String apiUrl = "https://api-cloud.browserstack.com/app-automate/sessions/" + sessionId + "/update_network.json";

        try {
            HttpClient client = HttpClient.newHttpClient();
            String modifiedProfileName = getBrowserStackProfileName(profileName.getValue().toString());

            // Prepare the request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("networkProfile", modifiedProfileName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic cnVrbWFuZ2FkYTE6UHpwelNGRUdOUVVhV1hwek5vazU=")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                logger.info("response code: " + statusCode);
            } else {
                logger.warn("Failed to switch to network profile. Response Status Code: " + statusCode + ", Response Body: " + response.body());
                setErrorMessage("Failed to switch to network configuration " + modifiedProfileName +" Response Status Code: " + statusCode );
                result = com.testsigma.sdk.Result.FAILED;
            }
        } catch (IOException | InterruptedException e) {
            logger.warn(errorMessage + e.getMessage());
            setErrorMessage(errorMessage + e.getMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }

        setSuccessMessage(successMessage + profileName.getValue().toString());
        return result;
    }

    private String getBrowserStackProfileName(String modifiedProfileName) {
        switch (modifiedProfileName) {
            case "no-network":
                modifiedProfileName = "no-network";
                break;
            case "2g":
                modifiedProfileName = "2g-gprs-good";
                break;
            case "3g":
                modifiedProfileName = "3g-umts-good";
                break;
            case "4g":
                modifiedProfileName = "4g-umts-good";
                break;
            case "default":
                modifiedProfileName = "reset";
                break;
        }
        return modifiedProfileName;
    }

}