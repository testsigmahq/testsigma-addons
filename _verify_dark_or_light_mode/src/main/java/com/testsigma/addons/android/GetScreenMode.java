package com.testsigma.addons.android;

import com.testsigma.sdk.AIRequest;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Verify if the current screen is in mode mode-name",
        description = "This action gets the screen mode of the android device",
        applicationType = ApplicationType.ANDROID)
public class GetScreenMode extends AndroidAction {
    @TestData(reference = "mode-name", allowedValues = {"dark", "light"})
    private com.testsigma.sdk.TestData modeName;
    @AI
    private com.testsigma.sdk.AI ai;

    @Override
    public Result execute() {
        try {
            String modeNameValue = modeName.getValue().toString().toLowerCase();
            // get the screen mode from the device
            String screenMode = getScreenMode();
            if (screenMode.equalsIgnoreCase(modeNameValue)) {
                logger.info("Screen mode is " + screenMode);
                setSuccessMessage("Successfully verified that the screen mode is <b>" + screenMode + "</b>");
                return Result.SUCCESS;
            } else if (screenMode.equalsIgnoreCase("unknown")) {
                logger.info("Screen mode is unknown");
                setErrorMessage("Screen mode Could not be determined");
                return Result.FAILED;
            } else {
                logger.info("Screen mode is not " + screenMode);
                setErrorMessage("Screen mode is <b?" + modeNameValue == "dark" ? "light" : "dark" + "</b>");
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("Error getting screen mode: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error getting screen mode: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    private String getScreenMode() {
        try {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            logger.info("Getting screen mode from the device");
            TakesScreenshot screenshot = (TakesScreenshot) androidDriver;
            logger.info("Created screenshot");
            File screenshotFile = screenshot.getScreenshotAs(OutputType.FILE);
            logger.info("Took screenshot");
            List<File> files = new ArrayList<>();
            files.add(screenshotFile);
            logger.info("Added screenshot to files");
            AIRequest aiRequest = new AIRequest();
            logger.info("Created AI request");
            aiRequest.setFiles(files);
            aiRequest.setPrompt("From the attached screenshot get the screen mode of the android device. The response should be either dark or light.");
            String response = ai.invokeAI(aiRequest);
            logger.info("Response is " + response);
            if (response.toLowerCase().contains("dark")) {
                return "dark";
            } else if (response.toLowerCase().contains("light")) {
                return "light";
            }
            return "unknown";
        } catch (Exception e) {
            logger.warn("Error getting screen mode: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error getting screen mode: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
