package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Data
@Action(actionText = "take screenshot of the current page and store the saved image file" +
        " path in runtime variable test-data",
        description = "validates options count in a select drop-down",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class TakeScreenShotAndStorePathInRuntime extends AndroidAction {
    @TestData(reference = "test-data", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        File file1;
        try {
            String basePdfDirectoryPath = String.valueOf(Files.createTempDirectory("basePdfDirectory"));
            logger.info("Base PDF Directory Path: " + basePdfDirectoryPath);
            String timeNow = String.valueOf(System.currentTimeMillis());
            file1 = new File(basePdfDirectoryPath + File.separator + "first_image_" + timeNow + ".png");
            // take screenshot and save it
            logger.info("Taking screenshot of the current page");
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            byte[] screenshot = ((TakesScreenshot) androidDriver).getScreenshotAs(OutputType.BYTES);
            logger.info("Screenshot taken successfully");
            saveBytesArrayToFile(file1.getAbsolutePath(), screenshot);
            logger.info("Screenshot saved at: " + file1.getAbsolutePath());
            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(file1.getAbsolutePath());
        } catch (IOException e) {
            logger.info("Error while creating temp directory: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Error while creating temp directory: " + e.getMessage());
        }
        setSuccessMessage("successfully stored the screenshot in S3 with URL: " + file1.getAbsolutePath());
        return result;
    }

    public void saveBytesArrayToFile(String filePath, byte[] imageBytes) {
        try {
            File file = new File(filePath);
            boolean isCreated = file.createNewFile();
            logger.debug("is file created - {}" + isCreated);
            FileUtils.writeByteArrayToFile(file, imageBytes);
        } catch (IOException e) {
            logger.info("Failed to save byte[] screenshot to path {}" + filePath);
        }
    }
}
