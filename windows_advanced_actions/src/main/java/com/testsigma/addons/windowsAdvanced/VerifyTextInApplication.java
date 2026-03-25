package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.OCRTextPoint;
import com.testsigma.addons.util.OCRUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

@Action(actionText = "verify that the text text-to-verify is present in opened application " +
        "and store result in runtime variable result-variable-name",
        description = "This action verifies that the specified text is present in the opened application" +
                " using OCR API capabilities. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Verify if text is present in the application and store result",
        useCustomScreenshot = true)
public class VerifyTextInApplication extends WindowsAdvancedAction {

    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "result-variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData1;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== OCR Text Verification: Starting Execution ===");

        try {
            String expectedText = testData.getValue().toString();
            logger.info("Looking for text: " + expectedText);

            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            File screenshotFile = saveScreenshotToFile(screenCapture, "application_screenshot");
            logger.info("Screenshot saved to: " + screenshotFile.getAbsolutePath());

            List<OCRTextPoint> textPoints = OCRUtils.extractTextPoints(screenshotFile, logger);
            logger.info("Found " + textPoints.size() + " text elements via OCR");

            boolean textFound = OCRUtils.searchForText(textPoints, expectedText, logger);

            ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);

            if (textFound) {
                logger.info("Text found in application. Step passed.");
                setSuccessMessage("Text '" + expectedText + "' was found in the application.");
                return Result.SUCCESS;
            } else {
                logger.debug("Text not found in application. Step failed.");
                setErrorMessage("Text '" + expectedText + "' was not found in the application.");
                return Result.FAILED;
            }

        } catch (Exception e) {
            logger.debug("Exception during OCR text verification: " + e.getMessage());
            setErrorMessage("Error during text verification: " + e.getMessage());
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "verify_text_failure_screenshot", logger);
            return Result.FAILED;
        }
    }

    private File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            logger.info("Screenshot saved to temporary file: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            logger.debug("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for processing.", e);
        }
    }
}
