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
import java.awt.image.BufferedImage;
import java.io.File;

@Data
@Action(actionText = " Verify that the image image-url is present on the screen",
        description = "Verify if the given image is present in current screen",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class SearchImage extends WindowsAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
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

            boolean isFound = response.getIsFound();
            int x1 = response.getX1();
            int y1 = response.getY1();
            int x2 = response.getX2();
            int y2 = response.getY2();

            if(!isFound){
                setErrorMessage("Given Image not found in the screen");
                return Result.FAILED;
            }
            setSuccessMessage("Given Image Found" +
                    " Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);

        }
        catch (Exception e){
            logger.info("Exception: "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while searching for given image");
            return Result.FAILED;
        }
        return Result.SUCCESS;

    }

}
