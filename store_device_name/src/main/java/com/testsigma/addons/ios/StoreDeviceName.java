package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AppiumCommandExecutor;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;
import java.util.NoSuchElementException;

@Data
@Action(actionText = "Testing v1 Store the device name in the runtime variable variable-name",
        description = "Retrieves the device name and stores it in the runtime variable.",
        applicationType = ApplicationType.IOS)
public class StoreDeviceName extends IOSAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    private static final String SAUCE_LABS_DEVICE_FIELD_NAME = "testobject_device_name";
    private static final String BROWSER_STACK_DEVICE_FIELD_NAME = "device";
    private static final String DEVICE_FIELD_IN_DESIRED_CAPS = "deviceName";
    private static final String DESIRED_CAPABILITY_NAME = "desired";

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution to store the device name.");
        Result result = Result.SUCCESS;
        String fullDeviceName = null;

        try {
            IOSDriver driver = (IOSDriver) this.driver;
            Capabilities capabilities = driver.getCapabilities();

            String environmentType = getEnvironmentType(capabilities);
            logger.info("Environment type: " + environmentType);

            if (!environmentType.equals("error")) {
                switch (environmentType) {
                    case "saucelabs":
                        fullDeviceName = getSauceLabsDeviceName(capabilities);
                        break;
                    case "browserstack":
                        fullDeviceName = getBrowserStackDeviceName();
                        break;
                    case "lambdatest":
                        fullDeviceName = getLambdaTestDeviceName(capabilities);
                        break;
                    case "local":
                        fullDeviceName = getDeviceNameFromDriverDesiredCaps(capabilities);
                        break;
                    default:
                        setErrorMessage("Unknown environment type: " + environmentType);
                        return Result.FAILED;
                }
            } else {
                setErrorMessage("Not able to determine the environment (cloud or local).");
                return Result.FAILED;
            }

            logger.info("Device name before version append: " + fullDeviceName);

            // Append iOS version — prefer desired.platformVersion (configured target)
            // over appium:platformVersion (what Appium reports back, may differ for beta OS)
            // Skip if the name already contains "iOS" (e.g. SauceLabs testobject_device_name)
            if (fullDeviceName != null && !fullDeviceName.toLowerCase().contains("ios")) {
                Map<String, Object> desiredCaps = (Map<String, Object>) capabilities.getCapability(DESIRED_CAPABILITY_NAME);
                String platformVersion = desiredCaps != null ? String.valueOf(desiredCaps.get("platformVersion")) : null;
                if (platformVersion == null || platformVersion.equals("null") || platformVersion.isEmpty()) {
                    platformVersion = (String) capabilities.getCapability("appium:platformVersion");
                }
                logger.info("Platform version: " + platformVersion);
                if (platformVersion != null && !platformVersion.isEmpty()) {
                    fullDeviceName = fullDeviceName + " iOS v" + platformVersion;
                }
            }

            if (fullDeviceName != null) {
                runtimeData.setValue(fullDeviceName);
                runtimeData.setKey(var.getValue().toString());
                setSuccessMessage("Successfully stored device name '" + fullDeviceName + "' in runtime variable: " + runtimeData.getKey());
                logger.info("Stored 'deviceName' : '" + fullDeviceName + "' into runtime variable: " + runtimeData.getKey());
            } else {
                result = Result.FAILED;
                setErrorMessage("Failed to retrieve device name.");
                logger.warn("Failed to retrieve device name.");
            }

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMsg = "Error occurred while trying to store the device name: " + ExceptionUtils.getStackTrace(e);
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

            // Tertiary: check BrowserStack-specific capability paths (iOS uses bootstrapPath, Android uses chromedriverExecutable)
            String chromedriverExec = (String) capabilities.getCapability("appium:chromedriverExecutable");
            String bootstrapPath = (String) capabilities.getCapability("appium:bootstrapPath");
            if ((chromedriverExec != null && chromedriverExec.toLowerCase().contains("browserstack"))
                    || (bootstrapPath != null && bootstrapPath.toLowerCase().contains("browserstack"))) {
                return "browserstack";
            }

            return "local";
        } catch (Exception e) {
            logger.info("Exception in getEnvironmentType: " + ExceptionUtils.getStackTrace(e));
            return "error";
        }
    }

    private String getLambdaTestDeviceName(Capabilities capabilities) {
        Map<String, Object> desiredCaps = (Map<String, Object>) capabilities.getCapability(DESIRED_CAPABILITY_NAME);
        String deviceName = desiredCaps != null ? (String) desiredCaps.get(DEVICE_FIELD_IN_DESIRED_CAPS) : null;

        // LambdaTest iOS may not expose a 'desired' wrapper — fall back to appium:deviceName
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = (String) capabilities.getCapability("appium:deviceName");
        }
        return deviceName;
    }

    private String getBrowserStackDeviceName() throws ParseException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        Object response = jse.executeScript("browserstack_executor: {\"action\": \"getSessionDetails\"}");
        JSONObject json = (JSONObject) new JSONParser().parse((String) response);
        String deviceName = (String) json.get(BROWSER_STACK_DEVICE_FIELD_NAME);
        String osVersion = (String) json.get("os_version");
        if (deviceName != null && osVersion != null && !osVersion.isEmpty()) {
            return deviceName + " iOS v" + osVersion;
        }
        return deviceName;
    }

    private String getSauceLabsDeviceName(Capabilities capabilities) {
        return (String) capabilities.getCapability(SAUCE_LABS_DEVICE_FIELD_NAME);
    }

    private String getDeviceNameFromDriverDesiredCaps(Capabilities capabilities) {
        Map<String, Object> desiredCaps = (Map<String, Object>) capabilities.getCapability(DESIRED_CAPABILITY_NAME);
        String deviceName = desiredCaps != null ? (String) desiredCaps.get(DEVICE_FIELD_IN_DESIRED_CAPS) : null;
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = (String) capabilities.getCapability("appium:deviceName");
        }
        return deviceName;
    }
}
