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

@Action(actionText = "Verify until the text testdata is present in the screen",
        description = "This action is meant to be used as the condition of a While Loop. On every check it " +
                "captures the screen and uses OCR to look for the given text. As long as the text is NOT " +
                "present, the action returns SUCCESS so the loop keeps iterating and waiting. The moment the " +
                "text is found (including on the very first check, if it was already present) the action " +
                "returns FAILED so the loop stops. This works only for local executions.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        actionType = StepActionType.WHILE_LOOP,
        displayName = "Verify until text is present in the screen",
        useCustomScreenshot = true)
public class VerifyUntilTextIsPresentWhileLoop extends WindowsAdvancedAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData;

  @TestStepResult
  private com.testsigma.sdk.TestStepResult testStepResult;

  @Override
  protected Result execute() throws NoSuchElementException {
    logger.info("=== Verify Until Text Is Present: Starting Execution ===");

    if (testData == null || testData.getValue() == null) {
      setErrorMessage("Text to verify is null");
      return Result.FAILED;
    }

    String expectedText = testData.getValue().toString();
    logger.info("Looking for text: " + expectedText);

    try {
      Robot robot = new Robot();
      Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      BufferedImage screenCapture = robot.createScreenCapture(screenRect);
      logger.info("Screen capture dimensions: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

      File screenshotFile = saveScreenshotToFile(screenCapture, "verify_until_text_present");
      logger.info("Screenshot saved to: " + screenshotFile.getAbsolutePath());

      List<OCRTextPoint> textPoints = OCRUtils.extractTextPoints(screenshotFile, logger);
      logger.info("Found " + textPoints.size() + " text elements via OCR");

      boolean textFound = OCRUtils.searchForText(textPoints, expectedText, logger);

      ScreenshotUtils.uploadScreenshotToS3(testStepResult, screenshotFile, logger);

      if (textFound) {

        logger.info("Text '" + expectedText + "' is present on screen. Loop condition is false, loop stops.");
        setErrorMessage("Expected text '" + expectedText + "' is present in the screen. Condition failed.");
        return Result.FAILED;
      } else {

        logger.info("Text '" + expectedText + "' is not present yet. Loop condition is true, loop continues.");
        setSuccessMessage("Expected text '" + expectedText + "' is not present in the screen. Condition passed.");
        return Result.SUCCESS;
      }

    } catch (Exception e) {
      logger.info("Exception during OCR text verification: " + e.getMessage());
      setErrorMessage("Error during text verification: " + e.getMessage());
      ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "verify_until_text_present_failure", logger);
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
      logger.info("Failed to save screenshot to file: " + e.getMessage());
      throw new RuntimeException("Unable to save screenshot for processing.", e);
    }
  }
}