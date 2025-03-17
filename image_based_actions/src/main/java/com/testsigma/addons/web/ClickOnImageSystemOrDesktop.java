package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;


@Data
@Action(actionText = "Click on image testdata on system/desktop window",
        description = "Click on the text using the text coordinates in system/desktop window",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)

public class ClickOnImageSystemOrDesktop extends WebAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData1;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            if (isCloudExecution()) {
                setErrorMessage("This action (Click on image testdata on system/desktop window) is not supported in cloud execution environments.");
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
            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon s3 url in which we are storing base image" + url);
            ocr.uploadFile(url, baseImageFile);
            logger.info("url: " + testStepResult.getScreenshotUrl());
            FindImageResponse responseObject = ocr.findImage(testData1.getValue().toString());

            if (responseObject.getIsFound()) {
                boolean isFound = responseObject.getIsFound();
                logger.info("Image found : " + isFound);
                int x1 = responseObject.getX1();
                int y1 = responseObject.getY1();
                int x2 = responseObject.getX2();
                int y2 = responseObject.getY2();
                clickOnCoordinates(x1, y1, x2, y2, width, height);
                setSuccessMessage("Image Found :" + isFound +
                        "    Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
                Thread.sleep(2000);

            } else {
                setErrorMessage("Unable to fetch the coordinates");
                result = Result.FAILED;
            }


        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while searching for the given image: " + e.getMessage()); // Include the exception message.
            result = Result.FAILED;
        }


        return result;
    }


    public void clickOnCoordinates(int x1, int y1, int x2, int y2, int imageWidth, int imageHeight) throws AWTException {
        Robot robot = new Robot();

        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private boolean isCloudExecution() {
        try {
            // If the driver is an instance of any driver than it is local
            if (driver instanceof ChromiumDriver || driver instanceof EdgeDriver || driver instanceof FirefoxDriver || driver instanceof SafariDriver) {
                return false;
            } else {
                //It's a cloud environment
                return true;
            }
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while checking for cloud execution: " + e.getMessage());
            return false;
        }
    }
}