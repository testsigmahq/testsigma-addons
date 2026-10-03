package com.testsigma.addons.windows;

import com.testsigma.addons.util.ImageMatchUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Multi-scale grayscale template matching using classical NCC (Normalized Cross-Correlation)
 * across multiple scales with histogram-equalized preprocessing.
 */
@Data
@Action(actionText = "Click on image image-url using template matching",
        description = "Clicks on image using multi-scale grayscale NCC template matching",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class ClickOnImageTemplateMatch extends WindowsAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageUrl;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final double TEMPLATE_MATCH_THRESHOLD = 0.55;

    @Override
    protected Result execute() {
        logger.info("=== ClickOnImageTemplateMatch: Starting ===");

        File screenshotFile = null;
        File targetImageFile = null;
        File highlightedFile = null;

        try {
            String imageUrlValue = imageUrl.getValue().toString();
            logger.info("Target image URL: " + imageUrlValue);

            targetImageFile = ImageMatchUtils.downloadImage("target_image", imageUrlValue, logger);
            logger.info("Target image prepared: " + targetImageFile.getAbsolutePath());

            Robot robot = new Robot();
            Dimension logicalScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int logicalWidth = logicalScreenSize.width;
            int logicalHeight = logicalScreenSize.height;
            logger.info("Logical screen: " + logicalWidth + "x" + logicalHeight);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            AffineTransform tx = gd.getDefaultConfiguration().getDefaultTransform();
            double displayScaleX = tx.getScaleX();
            double displayScaleY = tx.getScaleY();
            logger.info("Display scale: " + displayScaleX + "x" + displayScaleY);

            Rectangle screenRect = new Rectangle(logicalScreenSize);
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            logger.info("Capture dims: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

            screenshotFile = File.createTempFile("template_match_screenshot", ".png");
            ImageIO.write(screenCapture, "PNG", screenshotFile);

            int[] fileDims = ImageMatchUtils.getImageFileDimensions(screenshotFile);
            int fileWidth = fileDims[0];
            int fileHeight = fileDims[1];
            logger.info("PNG file dims: " + fileWidth + "x" + fileHeight);

            double scaleToLogicalX = (double) logicalWidth / fileWidth;
            double scaleToLogicalY = (double) logicalHeight / fileHeight;

            BufferedImage baseImage = ImageIO.read(screenshotFile);
            BufferedImage templateImage = ImageIO.read(targetImageFile);
            logger.info("Template dims: " + templateImage.getWidth() + "x" + templateImage.getHeight());

            int bw = baseImage.getWidth(), bh = baseImage.getHeight();
            int tw = templateImage.getWidth(), th = templateImage.getHeight();

            double[][] baseGray = ImageMatchUtils.toGrayscale(baseImage);
            double[][] tmplGray = ImageMatchUtils.toGrayscale(templateImage);

            long t0 = System.currentTimeMillis();
            ImageMatchUtils.MatchResult result = ImageMatchUtils.searchMultiScale(
                    baseGray, bw, bh, tmplGray, tw, th,
                    false, TEMPLATE_MATCH_THRESHOLD, "TemplateMatch", logger);
            logger.info("Template matching took " + (System.currentTimeMillis() - t0) + "ms — "
                    + (result.found ? "FOUND conf=" + result.confidence : "NOT FOUND: " + result.message));

            if (!result.found) {
                setErrorMessage("Template matching failed: " + result.message);
                ScreenshotUtils.uploadPlainScreenshot(screenshotFile, testStepResult, logger);
                return Result.FAILED;
            }

            highlightedFile = ScreenshotUtils.highlightAndUpload(
                    baseImage, result.x1, result.y1, result.x2, result.y2,
                    "template_match_highlighted", testStepResult, logger);

            int clickX = (int) (((result.x1 + result.x2) / 2.0) * scaleToLogicalX);
            int clickY = (int) (((result.y1 + result.y2) / 2.0) * scaleToLogicalY);
            logger.info("Clicking at (" + clickX + ", " + clickY + ") via " + result.method);

            robot.mouseMove(clickX, clickY);
            Thread.sleep(50);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(50);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(100);

            setSuccessMessage(String.format(
                    "Successfully clicked on image at coordinates: %d, %d  with confidence: %f",
                    clickX, clickY, result.confidence));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to click on Image. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            ImageMatchUtils.cleanupFile(screenshotFile);
            ImageMatchUtils.cleanupFile(targetImageFile);
            ImageMatchUtils.cleanupFile(highlightedFile);
        }
    }
}
