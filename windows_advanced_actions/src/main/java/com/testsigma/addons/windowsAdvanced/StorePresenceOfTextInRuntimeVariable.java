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


@Action(actionText = "verify that the text text-to-verify is present in opened application and" +
        " store result in runtime variable variable-to-store-result",
        description = "This action stores true if text is present in the screen else it stores false in the variable " +
                "using OCR API capabilities. " +
                "This works only for local executions",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        displayName = "Store the presence of text in runtime variable",
        useCustomScreenshot = true)
public class StorePresenceOfTextInRuntimeVariable extends WindowsAdvancedAction {

    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "variable-to-store-result", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData1;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

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
                runTimeData.setValue("true");
                runTimeData.setKey(testData1.getValue().toString());
            } else {
                logger.debug("Text not found in application. Step failed.");
                runTimeData.setValue("false");
                runTimeData.setKey(testData1.getValue().toString());
            }
        } catch (Exception e) {
            logger.debug("Exception during OCR text verification: " + e.getMessage());
            setErrorMessage("Error during text verification: " + e.getMessage());
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult,
                    "verify_text_failure_screenshot", logger);
            return Result.FAILED;
        }
        setSuccessMessage("Successfully stored the presence of the text " + testData.getValue().toString() +
                " in the variable " + testData1.getValue().toString());
        return Result.SUCCESS;
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
