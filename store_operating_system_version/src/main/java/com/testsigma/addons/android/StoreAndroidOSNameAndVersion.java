package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchElementException;
import lombok.Data;

@Data
@Action(
        actionText = "Store Android OS name and version in runtime variable variable-name",
        description = "Fetches the Android OS name and version (e.g., Android 13) and stores it in a runtime variable.",
        applicationType = ApplicationType.ANDROID
)
public class StoreAndroidOSNameAndVersion extends AndroidAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Fetching Android OS name and version...");
        Result result = Result.SUCCESS;

        try {
            AndroidDriver driver = (AndroidDriver) this.driver;
            Capabilities caps = driver.getCapabilities();

            String osVersion = (caps.getCapability("platformVersion") != null)
                    ? caps.getCapability("platformVersion").toString()
                    : "Unknown";

            String osInfo = "Android " + osVersion;

            runtimeData.setKey(variableName.getValue().toString());
            runtimeData.setValue(osInfo);

            setSuccessMessage("Stored OS info '" + osInfo + "' in runtime variable: " + runtimeData.getKey());
            logger.info("OS info: " + osInfo);

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Failed to retrieve Android OS version: " + e.getMessage());
            logger.warn("Failed to retrieve Android OS version: " + e.getMessage());
        }
        return result;
    }
}