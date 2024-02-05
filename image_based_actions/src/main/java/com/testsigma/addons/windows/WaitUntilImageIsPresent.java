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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

@Data
@Action(actionText = "Wait for test-data seconds until the image image-url is present on screen",
        description = "Wait until the given image is present on screen",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)

public class WaitUntilImageIsPresent extends WindowsAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData time;

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;


    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        String errorMessage = "Exception occurred while finding the text point";
        try{
            Robot robot = new Robot();
            long currentTimeMillis = System.currentTimeMillis();
            int givenTimeout = Integer.parseInt(time.getValue().toString());
            long endTimeMillis = currentTimeMillis + (givenTimeout * 1000L);
            int pollInterval = 5000;
            boolean isFound = false;
            while (currentTimeMillis <= endTimeMillis){

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

                isFound = response.getIsFound();
                int x1 = response.getX1();
                int y1 = response.getY1();
                int x2 = response.getX2();
                int y2 = response.getY2();
                if(isFound){
                    setSuccessMessage("Given Image Found" +
                            " Image coordinates :" + "x1-" + x1 + ", x2-" + x2 + ", y1-" + y1 + ", y2-" + y2);
                    break;
                } else{
                    Thread.sleep(pollInterval);
                }
                currentTimeMillis = System.currentTimeMillis();
            }
            if(!isFound){
                errorMessage = "Wait limit exceeded, text is not present";
                throw new Exception("Wait limit exceeded, text is not present");
            }
        } catch (Exception e){
            logger.info("Exception: "+ Arrays.toString(e.getStackTrace()));
            setErrorMessage(errorMessage);
            result = Result.FAILED;
        }

        return result;
    }

}
