package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(
        actionText = "Store OS name and version in runtime variable variable-name",
        description = "Fetches the OS name and version from the browser capabilities (e.g., Windows 10, macOS 13.0) and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB
)
public class StoreOSNameAndVersion extends WebAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() {
        logger.info("Fetching OS name and version from browser capabilities...");
        Result result = Result.SUCCESS;

        try {
            RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
            Capabilities caps = remoteDriver.getCapabilities();

            String platformName = "Unknown";
            String platformVersion = "Unknown";

            // Try BrowserStack's getSessionDetails unconditionally rather than pre-detecting the
            // environment: Testsigma's execution engine proxies the WebDriver connection, so neither
            // the command executor's remote address nor bstack:options survives to reveal BrowserStack
            // is actually in play (confirmed: both were absent even on a real BrowserStack session).
            // "browserstack_executor: {...}" is a harmless no-op (a JS labeled statement returning
            // null) on any other provider, so attempting it everywhere is safe.
            JSONObject bsSession = getBrowserStackSessionDetails();
            if (bsSession != null) {
                String bsOs = bsSession.optString("os", null);
                if (bsOs != null && !bsOs.isEmpty()) {
                    platformName = bsOs;
                }
                String bsOsVersion = bsSession.optString("os_version", null);
                if (bsOsVersion != null && !bsOsVersion.isEmpty()) {
                    platformVersion = bsOsVersion;
                }
            }

            // Fallback: prefer the raw capability, since getPlatformName() defaults to
            // Platform.ANY (not null) when the remote end never reported a real platform.
            if (platformName.equals("Unknown")) {
                try {
                    if (caps.getCapability("platformName") != null) {
                        platformName = caps.getCapability("platformName").toString();
                    } else if (caps.getPlatformName() != null && caps.getPlatformName() != Platform.ANY) {
                        platformName = caps.getPlatformName().toString();
                    }
                    if (platformName.equalsIgnoreCase("any")) {
                        platformName = "Unknown";
                    }
                } catch (Exception e) {
                    logger.warn("Could not get platformName from capabilities: " + e.getMessage());
                }
            }

            // Fallback: Sauce Labs / LambdaTest / BrowserStack nested capability maps, then plain capabilities
            if (platformVersion.equals("Unknown")) {
                platformVersion = getOSVersion(caps);
            }

            String osInfo = formatOSInfo(platformName, platformVersion);

            logger.warn("Platform Name detected: " + platformName);

            runtimeData.setKey(variableName.getValue().toString());
            runtimeData.setValue(osInfo);

            setSuccessMessage("Stored OS info '" + osInfo + "' in runtime variable: " + runtimeData.getKey());
            logger.info("OS info: " + osInfo);

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Failed to retrieve OS version: " + e.getMessage());
            logger.warn("Failed to retrieve OS version: " + e.getMessage());
        }
        return result;
    }

    // --- BrowserStack reports the true OS name/version via getSessionDetails ---
    private JSONObject getBrowserStackSessionDetails() {
        try {
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            Object response = jse.executeScript("browserstack_executor: {\"action\": \"getSessionDetails\"}");
            if (response == null) {
                return null;
            }
            return new JSONObject((String) response);
        } catch (Exception e) {
            // Expected on any non-BrowserStack provider: browserstack_executor is a magic string
            // BrowserStack's proxy intercepts before it reaches a real browser - elsewhere it hits
            // the actual JS engine and throws a SyntaxError. Not an error condition, so log briefly
            // rather than dumping Selenium's verbose WebDriverException message. Deliberately don't
            // name the provider here - which lab/cloud provider is in use is not surfaced in logs.
            logger.info("Cloud session details not available via JS executor: " + e.getClass().getSimpleName());
            return null;
        }
    }

    // --- OS version lookup with cloud lab support (Sauce Labs / LambdaTest / BrowserStack) ---
    private String getOSVersion(Capabilities caps) {
        // 1. Standard Selenium/Appium
        if (caps.getCapability("platformVersion") != null)
            return caps.getCapability("platformVersion").toString();

        // 2. BrowserStack
        if (caps.getCapability("osVersion") != null)
            return caps.getCapability("osVersion").toString();

        // 3. BrowserStack legacy
        if (caps.getCapability("os_version") != null)
            return caps.getCapability("os_version").toString();

        // 4. bstack:options
        Object bs = caps.getCapability("bstack:options");
        if (bs instanceof Map && ((Map<?, ?>) bs).get("osVersion") != null)
            return ((Map<?, ?>) bs).get("osVersion").toString();

        // 5. Sauce Labs
        Object sauce = caps.getCapability("sauce:options");
        if (sauce instanceof Map && ((Map<?, ?>) sauce).get("platformVersion") != null)
            return ((Map<?, ?>) sauce).get("platformVersion").toString();

        // 6. LambdaTest
        Object lt = caps.getCapability("lt:options");
        if (lt instanceof Map && ((Map<?, ?>) lt).get("platformVersion") != null)
            return ((Map<?, ?>) lt).get("platformVersion").toString();

        return "Unknown";
    }

    private String formatOSInfo(String platformName, String platformVersion) {
        if (platformName == null || platformName.equals("Unknown")) {
            return "Unknown";
        }

        String osName = platformName.toLowerCase();
        boolean hasVersion = platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown");

        // Some providers (e.g. Sauce Labs) resolve "platformName" as an already-complete string
        // like "Windows 11" rather than a bare family name + separate version capability. Don't
        // collapse that down to just "Windows" and lose the version it already carries.
        boolean nameAlreadyHasVersion = platformName.chars().anyMatch(Character::isDigit);

        if (osName.contains("mac") || osName.contains("darwin")) {
            if (hasVersion) {
                String majorVersion = platformVersion.split("\\.")[0];
                return "mac " + majorVersion;
            }
            return nameAlreadyHasVersion ? platformName : "mac";
        }

        if (osName.contains("win")) {
            if (hasVersion) {
                return "Windows " + platformVersion;
            }
            return nameAlreadyHasVersion ? platformName : "Windows";
        }

        if (osName.contains("linux")) {
            if (hasVersion) {
                return "Linux " + platformVersion;
            }
            return nameAlreadyHasVersion ? platformName : "Linux";
        }

        return hasVersion ? platformName + " " + platformVersion : platformName;
    }
}