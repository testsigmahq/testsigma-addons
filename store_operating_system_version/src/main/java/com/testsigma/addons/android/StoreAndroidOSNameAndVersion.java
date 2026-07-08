package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AppiumCommandExecutor;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;
import java.util.NoSuchElementException;

@Data
@Action(
        actionText = "Store Android OS name and version in runtime variable variable-name",
        description = "Fetches the Android OS name and version (e.g., Android 13) and stores it in a runtime variable.",
        applicationType = ApplicationType.ANDROID
)
public class StoreAndroidOSNameAndVersion extends AndroidAction {


    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    private static final String DESIRED_CAPABILITY_NAME = "desired";

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution to store the device OS version.");
        Result result = Result.SUCCESS;
        String osVersion = null;

        try {
            AndroidDriver driver = (AndroidDriver) this.driver;
            Capabilities capabilities = driver.getCapabilities();

            String environmentType = getEnvironmentType(capabilities);

            if (!environmentType.equals("error")) {
                switch (environmentType) {
                    case "browserstack":
                        osVersion = getBrowserStackOSVersion();
                        break;
                    case "saucelabs":
                    case "lambdatest":
                    case "local":
                    default:
                        osVersion = getOSVersionFromCapabilities(capabilities);
                        break;
                }
            } else {
                setErrorMessage("Not able to determine the environment (cloud or local).");
                return Result.FAILED;
            }

            // Fallback: if the environment-specific lookup came back empty, try capabilities directly
            if (osVersion == null || osVersion.isEmpty()) {
                osVersion = getOSVersionFromCapabilities(capabilities);
            }

            logger.info("OS version resolved: " + osVersion);

            if (osVersion != null && !osVersion.isEmpty()) {
                runtimeData.setValue(osVersion);
                runtimeData.setKey(var.getValue().toString());
                setSuccessMessage("Successfully stored OS version '" + osVersion + "' in runtime variable: " + runtimeData.getKey());
                logger.info("Stored 'osVersion' : '" + osVersion + "' into runtime variable: " + runtimeData.getKey());
            } else {
                result = Result.FAILED;
                setErrorMessage("Failed to retrieve OS version.");
                logger.warn("Failed to retrieve OS version.");
            }

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMsg = "Error occurred while trying to store the OS version: " + ExceptionUtils.getStackTrace(e);
            setErrorMessage(errorMsg);
            logger.warn(errorMsg);
        }

        return result;
    }

    private String getEnvironmentType(Capabilities capabilities) {
        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        try {
            Object commandExecutor = remoteDriver.getCommandExecutor();
            String remoteAddress;
            if (commandExecutor instanceof AppiumCommandExecutor) {
                remoteAddress = ((AppiumCommandExecutor) commandExecutor).getAddressOfRemoteServer().toString();
            } else {
                remoteAddress = "unknown";
            }

            // Primary: check remote URL (direct connections to cloud providers)
            String lowerAddress = remoteAddress.toLowerCase();
            if (lowerAddress.contains("lambdatest")) {
                return "lambdatest";
            } else if (lowerAddress.contains("browserstack")) {
                return "browserstack";
            } else if (lowerAddress.contains("saucelabs")) {
                return "saucelabs";
            }

            // Secondary: check session ID prefix (when routed through Testsigma's proxy)
            String sessionId = remoteDriver.getSessionId().toString();
            if (sessionId.toLowerCase().startsWith("lt:")) {
                return "lambdatest";
            } else if (sessionId.toLowerCase().startsWith("sl:")) {
                return "saucelabs";
            } else if (sessionId.toLowerCase().startsWith("bs:")) {
                return "browserstack";
            }

            // Tertiary: check chromedriver executable path in capabilities
            String chromedriverExec = (String) capabilities.getCapability("appium:chromedriverExecutable");
            if (chromedriverExec != null && chromedriverExec.toLowerCase().contains("browserstack")) {
                return "browserstack";
            }

            return "local";
        } catch (Exception e) {
            logger.info("Exception in getEnvironmentType: " + ExceptionUtils.getStackTrace(e));
            return "error";
        }
    }

    /**
     * BrowserStack reports the true OS version via getSessionDetails,
     * which is more reliable than the platformVersion capability.
     */
    private String getBrowserStackOSVersion() {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        Object response = jse.executeScript("browserstack_executor: {\"action\": \"getSessionDetails\"}");
        JSONObject json = new JSONObject((String) response);
        return json.optString("os_version", null);
    }

    private String getOSVersionFromCapabilities(Capabilities capabilities) {
        Map<String, Object> desiredCaps = (Map<String, Object>) capabilities.getCapability(DESIRED_CAPABILITY_NAME);
        String platformVersion = desiredCaps != null ? String.valueOf(desiredCaps.get("platformVersion")) : null;

        if (platformVersion == null || platformVersion.equals("null") || platformVersion.isEmpty()) {
            platformVersion = (String) capabilities.getCapability("appium:platformVersion");
        }
        return platformVersion;
    }
}