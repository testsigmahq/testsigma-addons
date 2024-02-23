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
@Action(actionText = "Tap on image image-url",
        description = "Tap on give image",
        applicationType = ApplicationType.ANDROID)
public class ClickOnImage extends AndroidAction {
    @TestData(reference = "image-url")
    private com.testsigma.sdk.TestData testData1;

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            AndroidDriver androidDriver = (AndroidDriver) this.driver;
            File baseImageFile = ((TakesScreenshot)androidDriver).getScreenshotAs(OutputType.FILE);
            BufferedImage bufferedImage = ImageIO.read(baseImageFile);
            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            logger.info("Width of image: " + imageWidth);
            logger.info("Height of image: " + imageHeight);
            Dimension dimension = androidDriver.manage().window().getSize();
            int screenWidth = dimension.width;
            int screenHeight = dimension.height;
            logger.info("Screen width: "+screenWidth);
            logger.info("Screen height: "+screenHeight);
            String url = testStepResult.getScreenshotUrl();
            logger.info("Amazon s3 url in which we are storing base image"+url);
            ocr.uploadFile(url, baseImageFile);
            FindImageResponse response = ocr.findImage(testData1.getValue().toString());
            if(response.getIsFound()) {
                logger.info("Image location found");
                logger.info("Image Found :" + response.getIsFound() +
                        "    Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());
                logger.info("Performing click..");
                int x = (response.getX1() + response.getX2()) / 2;
                int y = (response.getY1() + response.getY2()) / 2;
                logger.info("Screen shot based click locations: x="+x+"y="+y);
                double xRelative = ((double) x / imageWidth);
                double yRelative = ((double) y / imageHeight);
                logger.info("Error ratios: x relative: "+xRelative+" y relative: "+yRelative);
                int clickLocationX;
                int clickLocationY;
                if (Math.abs(imageWidth-screenWidth) > 5) {
                    clickLocationX = (int) (xRelative * screenWidth);
                    clickLocationY = (int) (yRelative * screenHeight);
                } else {
                    clickLocationX = x;
                    clickLocationY = y;
                }
                logger.info("Actual Click locations: x="+clickLocationX+"y="+clickLocationY);
                PointerInput FINGER = new PointerInput(TOUCH, "finger");
                Sequence tap = new Sequence(FINGER, 1)
                        .addAction(FINGER.createPointerMove(ofMillis(0), viewport(), clickLocationX, clickLocationY))
                        .addAction(FINGER.createPointerDown(LEFT.asArg()))
                        .addAction(new Pause(FINGER, ofMillis(2)))
                        .addAction(FINGER.createPointerUp(LEFT.asArg()));
                androidDriver.perform(Arrays.asList(tap));
                logger.info("CLick performed");
                setSuccessMessage("Image Found :" + response.getIsFound() +
                        "    Image coordinates :" + "x1-" + response.getX1() + ", x2-" + response.getX2() + ", y1-" + response.getY1() + ", y2-" + response.getY2());
                Thread.sleep(1000);
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
