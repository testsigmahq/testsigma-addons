package com.testsigma.addons.mobile_web.util;

import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Logger;
import com.testsigma.sdk.OCR;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

public class ClickActionUtils {
    private static void performClickWithFinger(int clickLocationX, int clickLocationY, AppiumDriver appiumDriver)
            throws Exception {
        try {
            PointerInput FINGER = new PointerInput(TOUCH, "finger");
            Sequence tap = new Sequence(FINGER, 1)
                    .addAction(FINGER.createPointerMove(ofMillis(0), viewport(), clickLocationX, clickLocationY))
                    .addAction(FINGER.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(FINGER, ofMillis(2)))
                    .addAction(FINGER.createPointerUp(LEFT.asArg()));
            appiumDriver.perform(Collections.singletonList(tap));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static String performClick(OCR ocr, String targetImgUrl, Float threshold,
                                      File baseImage, String screenshotUrl, AppiumDriver driver,
                                      Logger logger) throws Exception {

        try {
            FindImageResponse response = null;
            if(threshold == null){
                response = findImageAndGetResponse(ocr, targetImgUrl, baseImage, screenshotUrl);
            } else {
                response = findImageAndGetResponse(ocr, targetImgUrl, baseImage, screenshotUrl, threshold);
            }


            if (response.getIsFound()) {
                BufferedImage image = ImageIO.read(baseImage);
                int x1 = response.getX1();
                int x2 = response.getX2();
                int y1 = response.getY1();
                int y2 = response.getY2();

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                logger.info("imageWidth: " + imageWidth);
                logger.info("imageHeight: " + imageHeight);

                int xMean = ((x1 + x2) / 2);
                int yMean = ((y1 + y2) / 2);

                double xRelative = (double) xMean / imageWidth;
                double yRelative = (double) yMean / imageHeight;
                logger.info("xRelative: " + xRelative);
                logger.info("yRelative: " + yRelative);

                Dimension screenDimension = driver.manage().window().getSize();
                logger.info("screenDimension: " + screenDimension);

                int clickLocationX = (int) (xRelative * screenDimension.getWidth());
                int clickLocationY = (int) (yRelative * screenDimension.getHeight());
                logger.info(String.format("Click location: (%d,%d)", clickLocationX, clickLocationY));

                performClickWithFinger(clickLocationX,clickLocationY, driver);
                return String.format("Image Found: %s; Image coordinates: x1-%d, x2-%d, y1-%d, y2-%d",
                        response.getIsFound(), x1, x2, y1, y2);
            } else{
                throw new Exception("Unable to fetch the coordinates");
            }

        } catch (IOException e) {
            throw new Exception(e);
        }
    }


    private static FindImageResponse findImageAndGetResponse(OCR ocr, String targetImgUrl, File baseImage,
                                                             String screenshotUrl) {
        // upload base image to url
        ocr.uploadFile(screenshotUrl, baseImage);

        //find image
        return ocr.findImage(targetImgUrl);
    }

    private static FindImageResponse findImageAndGetResponse(OCR ocr, String targetImgUrl, File baseImage,
                                                             String screenshotUrl, Float threshold) {
        // upload base image to url
        ocr.uploadFile(screenshotUrl, baseImage);

        //find image
        return ocr.findImage(targetImgUrl, threshold);
    }
}
