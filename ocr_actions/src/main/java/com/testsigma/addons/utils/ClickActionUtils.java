package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;
import com.testsigma.sdk.OCR;
import com.testsigma.sdk.OCRTextPoint;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

public class ClickActionUtils {

    private static void performClickWithFinger(int clickLocationX, int clickLocationY, AppiumDriver appiumDriver, Logger logger)
            throws Exception {
        try {
            PointerInput FINGER = new PointerInput(TOUCH, "finger");
            Sequence tap = new Sequence(FINGER, 1)
                    .addAction(FINGER.createPointerMove(ofMillis(0), viewport(), clickLocationX, clickLocationY))
                    .addAction(FINGER.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(FINGER, ofMillis(20))) // Increased pause duration
                    .addAction(FINGER.createPointerUp(LEFT.asArg()));
            appiumDriver.perform(Collections.singletonList(tap));
        } catch (Exception e) {
            logger.warn("Error performing click with finger: " + e);  // Log the error
            throw new Exception("Error performing click with finger", e); // Provide a message
        }
    }

    public static String performClickText(OCR ocr,
                                          String text,
                                          File baseImage, AppiumDriver driver,
                                          Logger logger) throws Exception {

        try {
            BufferedImage bufferedImage = ImageIO.read(baseImage);

            if (bufferedImage == null) {
                return "Error: Could not read image from file: " + baseImage.getAbsolutePath();
            }

            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            logger.info("Width of image: " + imageWidth);
            logger.info("Height of image: " + imageHeight);
            Dimension dimension = driver.manage().window().getSize();
            int screenWidth = dimension.width;
            int screenHeight = dimension.height;
            logger.info("Screen width: " + screenWidth);
            logger.info("Screen height: " + screenHeight);
            List<OCRTextPoint> textPoints = ocr.extractTextFromPage();
            printAllCoordinates(textPoints, logger); // Pass logger
            OCRTextPoint textPoint = getTextPointFromText(textPoints, text); // Pass the text

            if (textPoint == null) {
                return String.format("Given text '%s' is not found", text);
            } else {
                logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                        ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                logger.info("Performing click..");
                int x = (textPoint.getX1() + textPoint.getX2()) / 2;
                int y = (textPoint.getY1() + textPoint.getY2()) / 2;
                logger.info("Screen shot based click locations: x="+x+"y="+y);
                double xRelative = ((double) x / imageWidth);
                double yRelative = ((double) y / imageHeight);
                logger.info("xRelative: " + xRelative);
                logger.info("yRelative: " + yRelative);

                int clickLocationX;
                int clickLocationY;

                if (driver instanceof AndroidDriver) {
                    if (Math.abs(imageWidth - screenWidth) > 20) {
                        clickLocationX = (int) (xRelative * screenWidth);
                        clickLocationY = (int) (yRelative * screenHeight);
                    } else {
                        clickLocationX = x;
                        clickLocationY = y;
                    }
                } else { // Assuming iOS or other driver where relative scaling is generally needed
                    Dimension screenDimension = driver.manage().window().getSize();
                    clickLocationX = (int) (xRelative * screenDimension.getWidth());
                    clickLocationY = (int) (yRelative * screenDimension.getHeight());
                }

                logger.info("Actual Click locations: x="+clickLocationX+"y="+clickLocationY);
                performClickWithFinger(clickLocationX, clickLocationY, driver, logger);
                return String.format("Click operation performed on the text " +
                        "    Text coordinates :" + "x1-" + textPoint.getX1() + ", x2-" + textPoint.getX2() + ", y1-" + textPoint.getY1() + ", y2-" + textPoint.getY2());
            }

        } catch (IOException e) {
            logger.warn("Error reading image: " + e);  // Log the error
            throw new IOException("Error reading image", e); // Rethrow as IOException
        }
    }

    public static String performClickTextOccurence(OCR ocr,
                                                   String text, int targetOccurence,
                                                   File baseImage, AppiumDriver driver,
                                                   Logger logger) throws Exception {

        try {
            BufferedImage bufferedImage = ImageIO.read(baseImage);

            if (bufferedImage == null) {
                return "Error: Could not read image from file: " + baseImage.getAbsolutePath();
            }

            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            logger.info("Width of image: " + imageWidth);
            logger.info("Height of image: " + imageHeight);
            Dimension dimension = driver.manage().window().getSize();
            int screenWidth = dimension.width;
            int screenHeight = dimension.height;
            logger.info("Screen width: " + screenWidth);
            logger.info("Screen height: " + screenHeight);
            List<OCRTextPoint> textPoints = ocr.extractTextFromPage();
            printAllCoordinates(textPoints, logger); // Pass logger

            // Count total occurrences of the text
            int totalOccurrences = 0;
            if (textPoints != null) {
                for (OCRTextPoint textPoint : textPoints) {
                    if (text.equals(textPoint.getText())) {
                        totalOccurrences++;
                    }
                }
            }

            if (targetOccurence > totalOccurrences) {
                return String.format("Occurrence '%s' is out of bounds. There are only '%s' occurrences of text '%s'", targetOccurence, totalOccurrences, text);
            }

            OCRTextPoint textPoint = getTextPointFromTextOccurence(textPoints, targetOccurence, text); // Pass the text

            if (textPoint == null) {
                return String.format("Given text '%s' with occurrence '%s' is not found", text, targetOccurence);
            } else {
                logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                        ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                logger.info("Performing click..");
                int x = (textPoint.getX1() + textPoint.getX2()) / 2;
                int y = (textPoint.getY1() + textPoint.getY2()) / 2;
                logger.info("Screen shot based click locations: x=" + x + "y=" + y);
                double xRelative = ((double) x / imageWidth);
                double yRelative = ((double) y / imageHeight);
                logger.info("xRelative: " + xRelative);
                logger.info("yRelative: " + yRelative);

                int clickLocationX;
                int clickLocationY;

                if (driver instanceof AndroidDriver) {
                    if (Math.abs(imageWidth - screenWidth) > 20) {
                        clickLocationX = (int) (xRelative * screenWidth);
                        clickLocationY = (int) (yRelative * screenHeight);
                    } else {
                        clickLocationX = x;
                        clickLocationY = y;
                    }
                } else { // Assuming iOS or other driver where relative scaling is generally needed
                    Dimension screenDimension = driver.manage().window().getSize();
                    clickLocationX = (int) (xRelative * screenDimension.getWidth());
                    clickLocationY = (int) (yRelative * screenDimension.getHeight());
                }


                logger.info("Actual Click locations: x=" + clickLocationX + "y=" + clickLocationY);
                performClickWithFinger(clickLocationX, clickLocationY, driver, logger);
                return String.format("Click operation performed on the text '%s' at occurrence '%s' Text coordinates :" +
                        "x1-%s, x2-%s, y1-%s, y2-%s", text, targetOccurence, textPoint.getX1(), textPoint.getX2(), textPoint.getY1(), textPoint.getY2());
            }

        } catch (IOException e) {
            logger.warn("Error reading image: " + e);  // Log the error
            throw new IOException("Error reading image", e); // Rethrow as IOException
        }
    }

    private static void printAllCoordinates(List<OCRTextPoint> textPoints, Logger logger) {
        if (textPoints != null) {
            for (OCRTextPoint textPoint : textPoints) {
                logger.info("text =" + textPoint.getText() + "x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() + "\n\n\n\n");
            }
        } else {
            logger.warn("Text points list is null, cannot print coordinates.");
        }
    }

    private static OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints, String text) {
        if(textPoints == null || text == null) {
            return null;
        }
        for(OCRTextPoint textPoint: textPoints) {
            if(text.equals(textPoint.getText())) {
                return textPoint;

            }
        }
        return  null;
    }

    private static OCRTextPoint getTextPointFromTextOccurence(List<OCRTextPoint> textPoints,int target_occurrence, String text) {
        if(textPoints == null) {
            return null;
        }
        int occurrences = 0;
        for(OCRTextPoint textPoint: textPoints) {
            if(text.equals(textPoint.getText())) {
                occurrences+=1;
                if(occurrences == target_occurrence){
                    return textPoint;
                }
            }
        }
        return  null;
    }
}