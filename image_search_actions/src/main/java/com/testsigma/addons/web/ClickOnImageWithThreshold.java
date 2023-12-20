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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Data
@Action(actionText = "Click on image with threshold",
        description = "Click on given image with threshold",
        applicationType = ApplicationType.WEB)
public class ClickOnImageWithThreshold extends WebAction {
    @TestData(reference = "image")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "threshold")
    private com.testsigma.sdk.TestData testData2;


    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        try {
            TakesScreenshot scrShot = ((TakesScreenshot) this.driver);
            File baseImageFile = scrShot.getScreenshotAs(OutputType.FILE);
            BufferedImage bufferedImage = ImageIO.read(baseImageFile);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            logger.info("Width of image: " + width);
            logger.info("Height of image: " + height);

            long innerHeight = (Long) ((JavascriptExecutor) driver).executeScript("return window.innerHeight;");
            long innerWidth = (Long) ((JavascriptExecutor) driver).executeScript("return window.innerWidth;");
            int windowHeight = (int) innerHeight;
            int windowWidth = (int) innerWidth;
            logger.info("inner Width of window: " + windowWidth);
            logger.info("inner Height of window: " + windowHeight);

            String url = testStepResult.getScreenshotUrl();
            ocr.uploadFile(url, baseImageFile);
            FindImageResponse response = ocr.findImage(testData1.getValue().toString(), Float.valueOf(testData2.getValue().toString()));
            int clickLocationX = (response.getX1() + response.getX2()) / 2;
            int clickLocationY = (response.getY1() + response.getY2()) / 2;

            double xRelative = (double) clickLocationX / width;
            double yRelative = (double) clickLocationY / height;

            int xCoordinate = (int) (xRelative * windowWidth);
            int yCoordinate = (int) (yRelative * windowHeight);
            logger.info("Click Location X: " + xCoordinate);
            logger.info("Click Location Y: " + yCoordinate);

            Actions actions = new Actions(driver);
            actions.moveByOffset(xCoordinate, yCoordinate).click().build().perform();
            setSuccessMessage("Image Found :" + response.getIsFound() +
                    "    Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());
            Thread.sleep(1000);
            return Result.SUCCESS;
        }
        catch (Exception e){
            logger.info("Exception: "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing click action");
            return Result.FAILED;
        }
    }
}