package com.testsigma.addons.windows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.NoSuchElementException;

@Data
@Action(
        actionText = "Mouse hover on image image-url with applied search threshold threshold-value (Ex: 0.9 , means 90% match), occurrence position found-at-position",
        description = "Mouse hover on given image with threshold at the given position",
        applicationType = ApplicationType.WINDOWS
)
public class HoverOnImageWithThresholdOccurrenceBased extends WindowsAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "found-at-position")
    private com.testsigma.sdk.TestData testData3;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            Robot robot = new Robot();

            // Capture current screen
            Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage tmp = robot.createScreenCapture(screenSize);

            // Save screenshot temporarily
            String tempDir = System.getProperty("java.io.tmpdir");
            String filename = "screenshot_" + System.currentTimeMillis() + ".jpg";
            String path = tempDir + filename;
            ImageIO.write(tmp, "jpg", new File(path));

            logger.info("Screen captured and saved at: " + path);
            logger.info("Screen dimensions: " + tmp.getWidth() + "x" + tmp.getHeight());

            File baseImageFile = new File(path);
            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon S3 URL where the base image is stored: " + url);
            Float threshold = Float.valueOf(testData2.getValue().toString());
            int occurrence = Integer.parseInt(testData3.getValue().toString());

            // Upload to OCR
            ocr.uploadFile(url, baseImageFile);
            FindImageResponse responseObject = ocr.findImage(testData1.getValue().toString(),occurrence,threshold);

            if (responseObject.getIsFound()) {
                int x1 = responseObject.getX1();
                int y1 = responseObject.getY1();
                int x2 = responseObject.getX2();
                int y2 = responseObject.getY2();

                int hoverX = (x1 + x2) / 2;
                int hoverY = (y1 + y2) / 2;

                logger.info("Hover location X: " + hoverX + ", Y: " + hoverY);

                // Perform hover (move mouse only)
                robot.mouseMove(hoverX, hoverY);

                setSuccessMessage("Mouse hovered successfully on image at coordinates: " +
                        "x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2);
                Thread.sleep(1500);
            } else {
                setErrorMessage("Image not found on screen.");
                result = Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing mouse hover action.");
            result = Result.FAILED;
        }
        return result;
    }
}
