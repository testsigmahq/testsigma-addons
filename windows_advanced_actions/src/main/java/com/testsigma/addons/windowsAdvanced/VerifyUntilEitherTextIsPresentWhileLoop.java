package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.util.OCRTextPoint;
import com.testsigma.addons.util.OCRUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

@Action(actionText = "Verify until text1 testdata1 condition testdata3 text2 testdata2 is present in the screen",
        description = "This action is meant to be used as the condition of a While Loop. On every check it " +
                "captures the screen and uses OCR to look for two given texts, combined using either an OR or " +
                "an AND condition selected from a dropdown (exact, case-sensitive match). " +
                "OR: the loop keeps iterating while NEITHER text is present, and stops the moment EITHER text " +
                "is found. " +
                "AND: the loop keeps iterating until BOTH texts are present together, and stops only once BOTH " +
                "are found. " +
                "This works only for local executions." + "Values are Case sensitve",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        actionType = StepActionType.WHILE_LOOP,
        displayName = "Verify until text1,text2 is present in the screen",
        useCustomScreenshot = true)
public class VerifyUntilEitherTextIsPresentWhileLoop extends WindowsAdvancedAction {

    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;


    @TestData(reference = "testdata3", allowedValues = {"AND", "OR"})
    private com.testsigma.sdk.TestData condition;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("=== Verify Until Text(s) Present (AND/OR): Starting Execution ===");

        if (testData1 == null || testData1.getValue() == null
                || testData2 == null || testData2.getValue() == null
                || condition == null || condition.getValue() == null) {
            setErrorMessage("One or more required inputs (text1, text2, condition) are null");
            return Result.FAILED;
        }

        String expectedText1 = testData1.getValue().toString();
        String expectedText2 = testData2.getValue().toString();
        String conditionType = condition.getValue().toString().trim();

        logger.info("Looking for text1 (exact match): " + expectedText1
                + " " + conditionType + " text2 (exact match): " + expectedText2);

        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            File screenshotFile = saveScreenshotToFile(screenCapture, "verify_until_text_present");
            logger.info("Screenshot saved to: " + screenshotFile.getAbsolutePath());

            List<OCRTextPoint> textPoints = OCRUtils.extractTextPoints(screenshotFile, logger);
            logger.info("Found " + textPoints.size() + " text elements via OCR");

            boolean text1Found = searchForExactText(textPoints, expectedText1);
            boolean text2Found = searchForExactText(textPoints, expectedText2);

            boolean conditionMet = "AND".equalsIgnoreCase(conditionType)
                    ? (text1Found && text2Found)
                    : (text1Found || text2Found);

            ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);

            if (conditionMet) {

                if ("AND".equalsIgnoreCase(conditionType)) {
                    logger.info("Both texts '" + expectedText1 + "' and '" + expectedText2
                            + "' are present on screen. Loop condition is false, loop stops.");
                    setErrorMessage("Both expected texts \"" + expectedText1 + "\" and \"" + expectedText2
                            + "\" are present in the screen.");
                } else {
                    String foundText = text1Found ? expectedText1 : expectedText2;
                    logger.info("Text '" + foundText + "' is present on screen. Loop condition is false, loop stops.");
                    setErrorMessage("One of the expected text \"" + foundText
                            + "\" is present in the screen.");
                }
                return Result.FAILED;

            } else {

                if ("AND".equalsIgnoreCase(conditionType)) {
                    logger.info("Both texts are not present together yet. Loop condition is true, loop continues.");
                    setSuccessMessage("Both expected texts (" + expectedText1 + " and " + expectedText2
                            + ") are not present together in the screen.");
                } else {
                    logger.info("Neither '" + expectedText1 + "' nor '" + expectedText2
                            + "' is present yet. Loop condition is true, loop continues.");
                    setSuccessMessage("Either expected texts (" + expectedText1 + " or " + expectedText2
                            + ") are not present in the screen.");
                }
                return Result.SUCCESS;
            }

        } catch (Exception e) {
            logger.info("Exception during OCR text verification: " + e.getMessage());
            setErrorMessage("Error during text verification: " + e.getMessage());
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "verify_until_text_present_failure", logger);
            return Result.FAILED;
        }
    }

    private boolean searchForExactText(List<OCRTextPoint> textPoints, String expectedText) {
        if (textPoints == null || expectedText == null) {
            return false;
        }
        for (OCRTextPoint point : textPoints) {
            if (point.getText() != null && point.getText().trim().equals(expectedText.trim())) {
                logger.info("Exact match found for: " + expectedText);
                return true;
            }
        }
        return false;
    }

    private File saveScreenshotToFile(BufferedImage screenshot, String fileName) throws Exception {
        try {
            File tempFile = File.createTempFile(fileName, ".png");
            ImageIO.write(screenshot, "PNG", tempFile);
            logger.info("Screenshot saved to temporary file: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            logger.info("Failed to save screenshot to file: " + e.getMessage());
            throw new RuntimeException("Unable to save screenshot for processing.", e);
        }
    }
}