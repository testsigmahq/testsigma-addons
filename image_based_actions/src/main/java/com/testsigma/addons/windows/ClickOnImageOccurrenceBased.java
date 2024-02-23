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
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

@Data
@Action(actionText = "Click on image image-url, occurrence position found-at-position",
        description = "Click on give image, at given position",
        applicationType = ApplicationType.WINDOWS)
public class ClickOnImageOccurrenceBased extends WindowsAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "found-at-position")
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
            int occurrence = Integer.parseInt(testData2.getValue().toString());
            FindImageResponse responseObject = ocr.findImage(testData1.getValue().toString(),occurrence);
            if (responseObject.getIsFound()){
                boolean isFound = responseObject.getIsFound();
                int x1 = responseObject.getX1();
                int y1 = responseObject.getY1();
                int x2 = responseObject.getX2();
                int y2 = responseObject.getY2();

                int clickLocationX = (x1 + x2) / 2;
                int clickLocationY = (y1 + y2) / 2;

                logger.info("Click Location X: " + clickLocationX);
                logger.info("Click Location Y: " + clickLocationY);

                robot.mouseMove(clickLocationX, clickLocationY);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                setSuccessMessage("Image Found :" + isFound +
                        "    Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
                Thread.sleep(2000);
            } else {
                setErrorMessage("Unable to fetch the coordinates");
                result = Result.FAILED;
            }
        }
        catch (Exception e){
            logger.info("Exception: "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing click action");
            result = Result.FAILED;
        }
        return result;
    }

}
