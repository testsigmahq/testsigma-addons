package com.testsigma.addons.windowslite;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

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

            String platformName = (caps.getCapability("platformName") != null)
                    ? caps.getCapability("platformName").toString()
                    : "Unknown";
            
            String platformVersion = (caps.getCapability("platformVersion") != null)
                    ? caps.getCapability("platformVersion").toString()
                    : "Unknown";

            String osInfo = platformName + " " + platformVersion;

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
}