package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(
        actionText = "Store device screen dimensions in runtime variable variable-name",
        description = "Retrieves the device screen width and height and stores it in a runtime variable in format widthxheight (e.g., 1080x1920).",
        applicationType = ApplicationType.IOS
)
public class StoreDeviceScreenDimensions extends IOSAction {

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Starting to fetch ios device screen dimensions...");
        Result result = Result.SUCCESS;

        try {
            IOSDriver iosDriver = (IOSDriver) this.driver;
            Dimension size = iosDriver.manage().window().getSize();
            logger.info("Size is: " + size.width + "x" + size.height);

            String dimensions = size.getWidth() + "x" + size.getHeight();
            runtimeData.setKey(variableName.getValue().toString());
            runtimeData.setValue(dimensions);

            setSuccessMessage("Successfully stored device dimensions '" + dimensions + "' in runtime variable: " + runtimeData.getKey());
            logger.info("Device dimensions: " + dimensions);

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMsg = "Failed to retrieve device dimensions: ";
            setErrorMessage(errorMsg +  ExceptionUtils.getMessage(e));
            logger.warn(errorMsg + ExceptionUtils.getStackTrace(e));
        }

        return result;
    }
}