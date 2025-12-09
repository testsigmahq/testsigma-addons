package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

            String platformName = (caps.getCapability("platformName") != null)
                    ? caps.getCapability("platformName").toString()
                    : "Unknown";
            
            String platformVersion = (caps.getCapability("platformVersion") != null)
                    ? caps.getCapability("platformVersion").toString()
                    : "Unknown";

            String osInfo = platformName + " " + platformVersion;

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
}