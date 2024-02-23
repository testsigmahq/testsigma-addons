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
import org.openqa.selenium.NoSuchElementException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Data
@Action(actionText = "Enter data test-data on the image image-url",
        description = "Enter given data on the given image ",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class EnterDataOnImage extends WindowsAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData2;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();


            // Fetch the Details of the Screen Size
            Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // Take the Snapshot of the Screen
            BufferedImage tmp = robot.createScreenCapture(screenSize);

            // Provide the destination details to copy the screenshot
            String tempDir = System.getProperty("java.io.tmpdir");
            String filename = "screenshot"+System.currentTimeMillis()+".jpg";
            String path = tempDir + filename;

            // To copy source image in to destination path
            ImageIO.write(tmp, "jpg",new File(path));
            int width = tmp.getWidth();
            int height = tmp.getHeight();
            logger.info("Width of image: " + width);
            logger.info("Height of image: " + height);

            File baseImageFile = new File(path);
            String url = testStepResult.getScreenshotUrl();
            ocr.uploadFile(url, baseImageFile);
            logger.info("url: "+ testStepResult.getScreenshotUrl());
            FindImageResponse response = ocr.findImage(testData1.getValue().toString());
            if (response.getIsFound()){
                boolean isFound = response.getIsFound();
                int x1 = response.getX1();
                int y1 = response.getY1();
                int x2 = response.getX2();
                int y2 = response.getY2();

                if(!isFound){
                    setErrorMessage("Given Image not found in the screen");
                    return Result.FAILED;
                }
                int clickLocationX = (x1 + x2) / 2;
                int clickLocationY = (y1 + y2) / 2;

                logger.info("Click Location X: " + clickLocationX);
                logger.info("Click Location Y: " + clickLocationY);

                // Move to the first coordinate (x, y)
                robot.mouseMove(clickLocationX, clickLocationY);

                // Simulate a mouse click
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);

                StringSelection stringSelection = new StringSelection(testData2.getValue().toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(null);
                clipboard.setContents(stringSelection, null);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                Thread.sleep(500);
                clipboard.setContents(data, null);
                setSuccessMessage("Given data entered successfully on the given image");
                Thread.sleep(1000);
            } else {
                setErrorMessage("Unable to fetch the coordinates");
                result = Result.FAILED;
            }
        }
        catch (Exception e){
            logger.info("Exception: "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while entering data into the given image");
            result = Result.FAILED;
        }
        return result;
    }
}
