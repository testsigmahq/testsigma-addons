package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import io.appium.java_client.ios.IOSDriver;
import lombok.Data;

@Data
@Action(
        actionText = "Store iOS device name and OS version in runtime variable variable-name",
        description = "Fetches the connected iOS device name and iOS version (e.g., iPhone 15 Pro - iOS 18.1) and stores it in the runtime variable.",
        applicationType = ApplicationType.IOS
)
public class StoreIOSNameAndVersion extends IOSAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() {
        logger.info("Starting to fetch iOS device name and OS version...");
        Result result = Result.SUCCESS;

        try {
            IOSDriver driver = (IOSDriver) this.driver;

            // Retrieve device name and OS version from capabilities
            String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
            String platformVersion = (String) driver.getCapabilities().getCapability("platformVersion");

            if (deviceName == null || platformVersion == null) {
                logger.info("Device name or platform version is null.");
                setErrorMessage("Could not retrieve device name or platform version from the iOS device.");
                return Result.FAILED;
            }
            String deviceInfo = deviceName + " - iOS " + platformVersion;
            logger.info("Detected device info: " + deviceInfo);

            // Store in runtime variable
            runtimeData = new com.testsigma.sdk.RunTimeData();
            runtimeData.setKey(variableName.getValue().toString());
            runtimeData.setValue(deviceInfo);
            logger.info("Successfully stored device info in runtime variable: " + runtimeData.getKey());
            setSuccessMessage("Successfully stored OS info <b>'" + deviceInfo + "'</b> in runtime variable: "
                    + runtimeData.getKey());
            logger.info("iOS device info successfully saved.");

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMsg = "Failed to retrieve iOS device info: " + e.getMessage();
            setErrorMessage(errorMsg);
            logger.warn(errorMsg);
        }

        return result;
    }
}