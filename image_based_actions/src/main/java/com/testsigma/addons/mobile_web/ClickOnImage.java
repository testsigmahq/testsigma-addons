package com.testsigma.addons.mobile_web;

import com.google.common.collect.ImmutableMap;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DriverCommand;

import java.io.File;

import static com.testsigma.addons.mobile_web.util.ClickActionUtils.performClick;
import static com.testsigma.addons.mobile_web.util.ContextUtils.getCurrentContext;

@Data
@Action(actionText = "Click on image image-url",
        description = "Click on given image",
        applicationType = ApplicationType.MOBILE_WEB)
public class ClickOnImage extends WebAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        AppiumDriver appiumDriver = (AppiumDriver) driver;
        String existingContext = getCurrentContext(appiumDriver);
        try {
            TakesScreenshot scrShot = ((TakesScreenshot) driver);
            File baseImageFile = scrShot.getScreenshotAs(OutputType.FILE);

            // Switch to native app context
            appiumDriver.execute(DriverCommand.SWITCH_TO_CONTEXT, ImmutableMap.of("name", "NATIVE_APP"));

            String successMessage= performClick(ocr, testData1.getValue().toString(), null,
                    baseImageFile,testStepResult.getScreenshotUrl(), appiumDriver, logger);

            setSuccessMessage(successMessage);

            // Switch back to old context
            appiumDriver.execute(DriverCommand.SWITCH_TO_CONTEXT, ImmutableMap.of("name", existingContext));

            return Result.SUCCESS;
        } catch (Exception e) {
            appiumDriver.execute(DriverCommand.SWITCH_TO_CONTEXT, ImmutableMap.of("name", existingContext));
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing click on image: " + e.getMessage());
            return Result.FAILED;
        }
    }
}