package com.testsigma.addons.windowslite;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(
        actionText = "Store OS name and version in runtime variable variable-name",
        description = "Fetches the OS name and version from the browser capabilities (e.g., Windows 10, macOS 13.0) and stores it in a runtime variable.",
        applicationType = ApplicationType.WINDOWS
)
public class StoreOSNameAndVersion extends WindowsAction {

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

            // Get platform name - try getPlatformName() method first, then capability
            String platformName = "Unknown";
            try {
                if (caps.getPlatformName() != null) {
                    platformName = caps.getPlatformName().toString();
                } else if (caps.getCapability("platformName") != null) {
                    platformName = caps.getCapability("platformName").toString();
                }
            } catch (Exception e) {
                logger.warn("Could not get platformName from capabilities: " + e.getMessage());
            }

            // Get platform version using vendor-compatible lookup
            String platformVersion = getOSVersion(caps);

            // Format OS info based on platform
            String osInfo = formatOSInfo(platformName, platformVersion);

            runtimeData = new com.testsigma.sdk.RunTimeData();
            runtimeData.setKey(variableName.getValue().toString());
            runtimeData.setValue(osInfo);

            setSuccessMessage("Stored OS info '" + osInfo + "' in runtime variable: " + runtimeData.getKey());
            logger.info("OS info: " + osInfo);

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Failed to retrieve OS version: " + e.getMessage());
            logger.warn("Failed to retrieve OS version: " + ExceptionUtils.getStackTrace(e));
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
        
        // Format Windows version
        if (osName.contains("win")) {
            if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
                return "Windows " + platformVersion;
            }
            return "Windows";
        }
        
        // Format Linux version
        if (osName.contains("linux")) {
            if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
                return "Linux " + platformVersion;
            }
            return "Linux";
        }
        
        // Default: return platform name and version as-is
        if (platformVersion != null && !platformVersion.isEmpty() && !platformVersion.equals("Unknown")) {
            return platformName + " " + platformVersion;
        }
        return platformName;
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
}