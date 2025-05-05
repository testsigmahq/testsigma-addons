package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.FindImageResponse;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Double click on the image image-url with threshold threshold-value",
        description = "Tap on give image",
        applicationType = ApplicationType.ANDROID)
public class DoubleClickOnImage extends AndroidAction {

    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData imageURL;

    @TestData(reference = "threshold-value")
    private com.testsigma.sdk.TestData threshold_;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        AndroidDriver androidDriver = (AndroidDriver) driver;
        try {
            logger.info("initiating execution");
            String threshold = threshold_.getValue().toString();
            int[] start_points_1 = getImageXYCoOrdinates(imageURL.getValue().toString(), threshold);
            if (start_points_1[0] == -1 && start_points_1[1] == -1) {
                setErrorMessage("Image not found.");
                return Result.FAILED;
            }
            int touchHoldTimeInt = 20;
            int touchGapInt = 30;
            logger.info("starting double tap");
            PointerInput pointer = new PointerInput(TOUCH, "finger");
            Sequence tap = new Sequence(pointer, 1)
                    .addAction(pointer.createPointerMove(ofMillis(0), viewport(), start_points_1[0], start_points_1[1]))
                    .addAction(pointer.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(pointer, ofMillis(touchHoldTimeInt)))
                    .addAction(pointer.createPointerUp(LEFT.asArg()))
                    .addAction(new Pause(pointer, ofMillis(touchGapInt)))
                    .addAction(pointer.createPointerDown(LEFT.asArg()))
                    .addAction(new Pause(pointer, ofMillis(touchHoldTimeInt)))
                    .addAction(pointer.createPointerUp(LEFT.asArg()));

            androidDriver.perform(Arrays.asList(tap));
            logger.info("successfully double clicked on the screen");
            setSuccessMessage("Successfully Double clicked on the given coordinates on the screen. X: "
                    + start_points_1[0] + ", Y: " + start_points_1[1]);
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing click action");
            result = Result.FAILED;
        }
        return result;
    }

    private int[] getImageXYCoOrdinates(String imageTestData, String threshold) {
        try {
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            File baseImageFile = ((TakesScreenshot) androidDriver).getScreenshotAs(OutputType.FILE);
            BufferedImage bufferedImage = ImageIO.read(baseImageFile);
            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            logger.info("Width of image: " + imageWidth);
            logger.info("Height of image: " + imageHeight);
            Dimension dimension = androidDriver.manage().window().getSize();
            int screenWidth = dimension.width;
            int screenHeight = dimension.height;
            logger.info("Screen width: " + screenWidth);
            logger.info("Screen height: " + screenHeight);
            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon s3 url in which we are storing base image " + url);
            ocr.uploadFile(url, baseImageFile);
            FindImageResponse response = ocr.findImage(imageTestData, Float.valueOf(threshold));
            if (response != null && response.getIsFound()) {
                logger.info("Image location found");
                logger.info("Image Found :" + response.getIsFound() +
                        "    Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-"
                        + response.getY1() + ", y2-" + response.getY2());
                logger.info("Performing click..");
                int x = (response.getX1() + response.getX2()) / 2;
                int y = (response.getY1() + response.getY2()) / 2;
                logger.info("Screen shot based click locations: x=" + x + "y=" + y);
                double xRelative = ((double) x / imageWidth);
                double yRelative = ((double) y / imageHeight);
                logger.info("Error ratios: x relative: " + xRelative + " y relative: " + yRelative);
                int clickLocationX;
                int clickLocationY;
                if (Math.abs(imageWidth - screenWidth) > 20) {
                    clickLocationX = (int) (xRelative * screenWidth);
                    clickLocationY = (int) (yRelative * screenHeight);
                } else {
                    clickLocationX = x;
                    clickLocationY = y;
                }
                return new int[]{clickLocationX, clickLocationY};
            } else {
                setErrorMessage("Unable to fetch the coordinates");
                return new int[]{-1, -1};
            }
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing click action");
            return new int[]{-1, -1};
        }
    }

}
