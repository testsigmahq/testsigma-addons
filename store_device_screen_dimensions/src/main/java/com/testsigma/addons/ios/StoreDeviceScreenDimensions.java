package com.testsigma.addons.ios;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;

import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;

import io.appium.java_client.ios.IOSDriver;
import lombok.Data;

@Data
@Action(
        actionText = "Store device screen dimensions in runtime variable variable-name",
        description = "Retrieves the iOS device's physical screen width and height (in pixels) and stores it in the runtime variable in format widthxheight (e.g., 1668x2388).",
        applicationType = ApplicationType.IOS
)
public class StoreDeviceScreenDimensions extends IOSAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Starting to fetch iOS device screen dimensions...");
        Result result = Result.SUCCESS;

        try {
            IOSDriver driver = (IOSDriver) this.driver;
            Dimension size = driver.manage().window().getSize();

            int logicalWidth = size.getWidth();
            int logicalHeight = size.getHeight();
            logger.info("Logical screen size: " + logicalWidth + "x" + logicalHeight);

            // Determine Retina scale factor dynamically
            int scaleFactor = getRetinaScaleFactor(driver, logicalWidth);
            logger.info("Scale factor: " + scaleFactor);

            int physicalWidth = logicalWidth * scaleFactor;
            int physicalHeight = logicalHeight * scaleFactor;

            String dimensions = physicalWidth + "x" + physicalHeight;
            logger.info("Dimensions: " + dimensions);

            runtimeData.setKey(variableName.getValue().toString());
            runtimeData.setValue(dimensions);

            setSuccessMessage("Successfully stored iOS device dimensions '" + dimensions + "' in runtime variable: " + runtimeData.getKey());
            logger.info("Physical device resolution: " + dimensions + " (scale factor: " + scaleFactor + "x)");

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMsg = "Failed to retrieve iOS device dimensions: " + ExceptionUtils.getStackTrace(e);
            setErrorMessage(errorMsg);
            logger.warn(errorMsg);
        }

        return result;
    }

    private int getRetinaScaleFactor(IOSDriver driver, int logicalWidth) {
        try {
            String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
            logger.info("Device Name from capabilities: " + deviceName);

            if (deviceName != null) {
                String lower = deviceName.toLowerCase();
                if (lower.contains("ipad")) {
                    return 2;
                } else if (lower.contains("iphone")) {
                    return 3;
                }
            }

            if (logicalWidth < 1000) {
                return 3;
            } else {
                return 2;
            }

        } catch (Exception e) {
            logger.warn("Could not determine device scale factor. Defaulting to 2x. " + ExceptionUtils.getStackTrace(e));
            return 2;
        }
    }
}
