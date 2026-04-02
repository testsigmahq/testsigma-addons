package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Data
@Action(actionText = "Click on text testdata on system/desktop window", description = "Click on the text using the text coordinates in system/desktop window", applicationType = ApplicationType.WEB, useCustomScreenshot = true)

public class ClickOnTextSystemOrDesktop extends WebAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData text;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            if (isCloudExecution()) {
                setErrorMessage(
                        "This action (Click on text testdata on system/desktop window) is not supported in cloud execution environments.");
                result = Result.FAILED;
                return result;
            }
            Robot robot = new Robot();

            // Fetch the Details of the Screen Size
            Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // Take the Snapshot of the Screen
            BufferedImage tmp = robot.createScreenCapture(screenSize);

            // Provide the destination details to copy the screenshot
            String tempDir = System.getProperty("java.io.tmpdir");
            String filename = "screenshot" + System.currentTimeMillis() + ".jpg";
            String path = tempDir + filename;
            logger.info("Path: " + path);

            // To copy source image in to destination path
            ImageIO.write(tmp, "jpg", new File(path));
            int width = tmp.getWidth();
            int height = tmp.getHeight();
            logger.info("Width of image: " + width);
            logger.info("Height of image: " + height);

            File baseImageFile = new File(path);
            OCRImage ocrImage = new OCRImage();
            ocrImage.setOcrImageFile(baseImageFile);

            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
            printAllCoordinates(textPoints);
            OCRTextPoint textPoint = getTextPointFromText(textPoints);
            if (textPoint == null) {
                result = Result.FAILED;
                setErrorMessage("Given text is not found");

            } else {
                logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                        ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                clickOnCoordinates(textPoint, width, height);
                tmp = robot.createScreenCapture(screenSize);
                filename = "screenshot" + System.currentTimeMillis() + ".jpg";
                path = tempDir + filename;
                ImageIO.write(tmp, "jpg", new File(path));
                baseImageFile = new File(path);
                String url = testStepResult.getScreenshotUrl();
                ocr.uploadFile(url, baseImageFile);
                setSuccessMessage("Click operation performed on the text " +
                        "    Text coordinates :" + "x-" + (int) xrelative + ", y-" + (int) yrelative);
            }
        } catch (Exception e) {
            logger.info("Exception: " + Arrays.toString(e.getStackTrace()));
            setErrorMessage("Exception occurred while searching for the given text");
            result = Result.FAILED;
        }

        return result;
    }

    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints) {
        if (textPoints == null) {
            return null;
        }
        for (OCRTextPoint textPoint : textPoints) {
            if (text.getValue().equals(textPoint.getText())) {
                return textPoint;

            }
        }
        return null;
    }

    private void printAllCoordinates(List<OCRTextPoint> textPoints) {
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("text =" + textPoint.getText() + "x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1()
                    + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() + "\n\n\n\n");
        }
    }

    private double xrelative;
    private double yrelative;

    public void clickOnCoordinates(OCRTextPoint textPoint, int imageWidth, int imageHeight) throws AWTException {
        Robot robot = new Robot();

        int x1 = textPoint.getX1();
        int y1 = textPoint.getY1();
        int x2 = textPoint.getX2();
        int y2 = textPoint.getY2();

        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        logger.info("MEAN X coordinate: " + x + "\n");
        logger.info("MEAN Y coordinate: " + y + "\n");

        JavascriptExecutor js = (JavascriptExecutor) driver;

        long browserHeight = (Long) js.executeScript("return window.innerHeight;");
        long browserWidth = (Long) js.executeScript("return window.innerWidth;");

        xrelative = ((double) x / (double) browserWidth) * (double) imageWidth;
        yrelative = ((double) y / (double) browserHeight) * (double) imageHeight;

        logger.info("X relative: " + (int) xrelative + "\n");
        logger.info("Y relative: " + (int) yrelative + "\n");

        robot.mouseMove((int) xrelative, (int) yrelative);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private boolean isCloudExecution() {
        try {
            // If the driver is an instance of any driver than it is local
            if (driver instanceof ChromiumDriver || driver instanceof EdgeDriver || driver instanceof FirefoxDriver
                    || driver instanceof SafariDriver) {
                return false;
            } else {
                // It's a cloud environment
                return true;
            }
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while checking for cloud execution: " + e.getMessage());
            return false;
        }
    }

}
