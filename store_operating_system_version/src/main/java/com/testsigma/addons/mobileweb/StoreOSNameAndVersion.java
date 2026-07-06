package com.testsigma.addons.mobileweb;

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
        description = "Fetches the OS name and version from the mobile web browser capabilities (e.g., Android 13, iOS 16.0) and stores it in a runtime variable.",
        applicationType = ApplicationType.MOBILE_WEB
)
public class StoreOSNameAndVersion extends WebAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() {
        logger.info("Fetching OS name and version from mobile web browser capabilities...");
        Result result = Result.SUCCESS;

        try {
            RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
            Capabilities caps = remoteDriver.getCapabilities();

            String platformName = "Unknown";
            String platformVersion = "Unknown";

            // Try BrowserStack's getSessionDetails unconditionally rather than pre-detecting the
            // environment: Testsigma's execution engine proxies the WebDriver connection, so neither
            // the command executor's remote address nor bstack:options survives to reveal BrowserStack
            // is actually in play (confirmed on a real BrowserStack session - both were absent).
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

            // Fallback: try JavaScript executor, then capabilities
            if (platformVersion.equals("Unknown")) {
                platformVersion = getOSVersionFromJS(remoteDriver);
                if (platformVersion == null || platformVersion.isEmpty() || platformVersion.equals("Unknown")) {
                    platformVersion = getOSVersion(caps);
                }
            }

            // Format OS info based on platform
            String osInfo = formatOSInfo(platformName, platformVersion);

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

    private String formatOSInfo(String platformName, String platformVersion) {
        if (platformName == null || platformName.equals("Unknown")) {
            return "Unknown";
        }

        String osName = platformName.toLowerCase();

        // Format macOS version (e.g., "mac 15" for macOS 15.x)
        if (osName.contains("mac") || osName.contains("darwin")) {
            if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
                // Extract major version number (e.g., "15.0" -> "15", "13.6" -> "13")
                String majorVersion = platformVersion.split("\\.")[0];
                return "mac " + majorVersion;
            }
            return "mac";
        }

        // Format Android version
        if (osName.contains("android")) {
            if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
                return "Android " + platformVersion;
            }
            return "Android";
        }

        // Format iOS version
        if (osName.contains("ios")) {
            if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
                return "iOS " + platformVersion;
            }
            return "iOS";
        }

        // Format Windows version
        if (osName.contains("win")) {
            if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
                return "Windows " + platformVersion;
            }
            return "Windows";
        }

        // Default: return platform name and version as-is
        if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
            return platformName + " " + platformVersion;
        }
        return platformName;
    }

    // --- Get OS version using JavaScript executor ---
    private String getOSVersionFromJS(RemoteWebDriver driver) {
        try {
            if (driver instanceof JavascriptExecutor) {
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // JavaScript to extract OS version from user agent
                String jsCode = "var ua = navigator.userAgent || navigator.vendor || window.opera; " +
                        "var os = 'Unknown'; " +
                        "var version = 'Unknown'; " +
                        "if (ua.indexOf('Mac OS X') !== -1) { " +
                        "  os = 'mac'; " +
                        "  var match = ua.match(/Mac OS X (\\d+)[._](\\d+)/); " +
                        "  if (match) version = match[1]; " +
                        "} else if (ua.indexOf('Windows') !== -1) { " +
                        "  os = 'Windows'; " +
                        "  var match = ua.match(/Windows NT (\\d+)\\.(\\d+)/); " +
                        "  if (match) { " +
                        "    var major = parseInt(match[1]); " +
                        "    var minor = parseInt(match[2]); " +
                        "    if (major === 10 && minor === 0) version = '10'; " +
                        "    else if (major === 6 && minor === 1) version = '7'; " +
                        "    else if (major === 6 && minor === 2) version = '8'; " +
                        "    else if (major === 6 && minor === 3) version = '8.1'; " +
                        "    else version = major + '.' + minor; " +
                        "  } " +
                        "} else if (ua.indexOf('Linux') !== -1) { " +
                        "  os = 'Linux'; " +
                        "  var match = ua.match(/Linux[^\\d]*(\\d+[\\.\\d]*)/); " +
                        "  if (match) version = match[1]; " +
                        "} else if (ua.indexOf('Android') !== -1) { " +
                        "  os = 'Android'; " +
                        "  var match = ua.match(/Android (\\d+[\\.\\d]*)/); " +
                        "  if (match) version = match[1]; " +
                        "} else if (/iPad|iPhone|iPod/.test(ua)) { " +
                        "  os = 'iOS'; " +
                        "  var match = ua.match(/OS (\\d+)[._](\\d+)/); " +
                        "  if (match) version = match[1] + '.' + match[2]; " +
                        "} " +
                        "return os + '|' + version;";

                Object result = js.executeScript(jsCode);
                if (result != null) {
                    String[] parts = result.toString().split("\\|");
                    if (parts.length == 2 && !parts[1].equals("Unknown")) {
                        logger.info("OS version from JavaScript: " + parts[0] + " " + parts[1]);
                        return parts[1];
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get OS version from JavaScript: " + e.getMessage());
        }
        return "Unknown";
    }

    // --- OS version lookup with cloud lab support ---
    private String getOSVersion(Capabilities caps) {
        // 1. Standard Appium/Selenium
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

        // 7. System properties as fallback
        String osVersion = System.getProperty("os.version");
        if (osVersion != null && !osVersion.isEmpty())
            return osVersion;

        return "Unknown";
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
            logger.info("Cloud session details not available via JS executor: " + e.getClass().getSimpleName());
            return null;
        }
    }
}