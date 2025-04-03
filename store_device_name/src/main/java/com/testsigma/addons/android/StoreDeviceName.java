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
import org.apache.commons.lang3.StringUtils;
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
@Action(actionText = "Store the device name in the runtime variable variable-name",
        description = "Retrieves the device name and stores it in the runtime variable.",
        applicationType = ApplicationType.ANDROID)
public class StoreDeviceName extends AndroidAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    private static final String SAUCE_LABS_DEVICE_FIELD_NAME = "testobject_device_name";
    private static final String LAMBDA_TEST_DEVICE_FIELD_NAME = "deviceName";
    private static final String BROWSER_STACK_DEVICE_FIELD_NAME = "device";
    private static final String DEVICE_FIELD_IN_DESIRED_CAPS = "deviceName";

    private static final String DESIRED_CAPABILITY_NAME = "desired";

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution to store the device name.");
        Result result = Result.SUCCESS;
        String fullDeviceName = null;

        try {
            AndroidDriver driver = (AndroidDriver) this.driver;
            Capabilities capabilities = driver.getCapabilities();
            String environmentType = getEnvironmentType();
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

            // Store in runtime variable
            if (fullDeviceName != null) {
                runtimeData.setValue(fullDeviceName);
                runtimeData.setKey(var.getValue().toString());

                setSuccessMessage("Successfully stored deviceName '" + fullDeviceName + "' in runtime variable: " + runtimeData.getKey());
                logger.info("Stored 'deviceName' : '" + fullDeviceName + "' into runtime variable: " + runtimeData.getKey());
            } else {
                result = Result.FAILED;
                setErrorMessage("Failed to retrieve device name.");
                logger.warn("Failed to retrieve device name.");
            }

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMsg = "Error occurred while trying to store the device Name : " + ExceptionUtils.getStackTrace(e);
            setErrorMessage(errorMsg);
            logger.warn(errorMsg);
        }

        return result;
    }

    private String getEnvironmentType() {
        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        String remoteAddress;
        String environmentType = null;
        try {
            Object commandExecutor = remoteDriver.getCommandExecutor();
            if (commandExecutor instanceof AppiumCommandExecutor) {
                AppiumCommandExecutor appiumExecutor = (AppiumCommandExecutor) commandExecutor;
                remoteAddress = appiumExecutor.getAddressOfRemoteServer().toString();
            } else {
                remoteAddress = "unknown";
            }

            if (remoteAddress.toLowerCase().contains("lambdatest")) {
                environmentType = "lambdatest";
            } else if (remoteAddress.toLowerCase().contains("browserstack")) {
                environmentType = "browserstack";
            } else if (remoteAddress.toLowerCase().contains("saucelabs")) {
                environmentType = "saucelabs";
            } else {
                environmentType = "local";
            }
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            return "error";
        }

        return environmentType;
    }

    private String getLambdaTestDeviceName(Capabilities capabilities) {
        String deviceManufacturer = (String) capabilities.getCapability("appium:deviceManufacturer");

        // Make deviceManufacturer value first letter capital
        if (deviceManufacturer != null && !deviceManufacturer.isEmpty()) {
            deviceManufacturer = StringUtils.capitalize(deviceManufacturer);
        }

        Map<String, Object> desiredCaps = (Map<String, Object>) capabilities.getCapability(DESIRED_CAPABILITY_NAME);
        String deviceName = desiredCaps != null ? (String) desiredCaps.get(DEVICE_FIELD_IN_DESIRED_CAPS) : null;

        if (deviceManufacturer != null && deviceName != null) {
            return deviceManufacturer + " " + deviceName;
        }
        return null;
    }

    private String getBrowserStackDeviceName() throws ParseException {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        Object response = jse.executeScript("browserstack_executor: {\"action\": \"getSessionDetails\"}");
        JSONObject json = (JSONObject) new JSONParser().parse((String) response);
        return (String) json.get(BROWSER_STACK_DEVICE_FIELD_NAME);
    }

    private String getSauceLabsDeviceName(Capabilities capabilities) {
        return (String) capabilities.getCapability(SAUCE_LABS_DEVICE_FIELD_NAME);
    }

    private String getDeviceNameFromDriverDesiredCaps(Capabilities capabilities) {
        Map<String, Object> desiredCaps = (Map<String, Object>) capabilities.getCapability(DESIRED_CAPABILITY_NAME);
        return desiredCaps != null ? (String) desiredCaps.get(DEVICE_FIELD_IN_DESIRED_CAPS) : null;
    }
}