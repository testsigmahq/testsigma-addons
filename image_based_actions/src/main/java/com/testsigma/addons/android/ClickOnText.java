package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofMillis;
import static org.openqa.selenium.interactions.PointerInput.Kind.TOUCH;
import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Data
@Action(actionText = "Tap on text name",
        description = "Tap on the text using the text coordinates",
        applicationType = ApplicationType.ANDROID)

public class ClickOnText extends AndroidAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "name")
    private com.testsigma.sdk.TestData text;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
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
            List<OCRTextPoint> textPoints = ocr.extractTextFromPage();
            printAllCoordinates(textPoints);
            OCRTextPoint textPoint = getTextPointFromText(textPoints);
            if (textPoint == null) {
                result = Result.FAILED;
                setErrorMessage("Given text is not found");

            } else {
                logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                        ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                logger.info("Performing click..");
                int x = (textPoint.getX1() + textPoint.getX2()) / 2;
                int y = (textPoint.getY1() + textPoint.getY2()) / 2;
                logger.info("Screen shot based click locations: x="+x+"y="+y);
                double xRelative = ((double) x / imageWidth);
                double yRelative = ((double) y / imageHeight);
                logger.info("Error ratios: x relative: "+xRelative+" y relative: "+yRelative);
                int clickLocationX;
                int clickLocationY;
                if(Math.abs(imageWidth-screenWidth) > 20){
                    clickLocationX = (int) (xRelative * screenWidth);
                    clickLocationY = (int) (yRelative * screenHeight);
                } else {
                    clickLocationX = x;
                    clickLocationY = y;
                }
                logger.info("Actual Click locations: x="+clickLocationX+"y="+clickLocationY);
                clickOnCoordinates(clickLocationX, clickLocationY);
                setSuccessMessage("Click operation performed on the text " +
                        "    Text coordinates :" + "x1-" + textPoint.getX1() + ", x2-" + textPoint.getX2() + ", y1-" + textPoint.getY1() + ", y2-" + textPoint.getY2());
            }
        } catch(Exception e) {
            logger.info("Exception: "+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while performing click action");
            result = Result.FAILED;
        }
        return result;
    }
    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints) {
        if(textPoints == null) {
            return null;
        }
        for(OCRTextPoint textPoint: textPoints) {
            if(text.getValue().equals(textPoint.getText())) {
                return textPoint;

            }
        }
        return  null;
    }

    private void printAllCoordinates(List<OCRTextPoint> textPoints) {
        for(OCRTextPoint textPoint: textPoints) {
            logger.info("text =" + textPoint.getText() + "x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1()  + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() +"\n\n\n\n");
        }
    }
    public void clickOnCoordinates(int clickX, int clickY) {

        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        PointerInput FINGER = new PointerInput(TOUCH, "finger");
        Sequence tap = new Sequence(FINGER, 1)
                .addAction(FINGER.createPointerMove(ofMillis(0), viewport(), clickX, clickY))
                .addAction(FINGER.createPointerDown(LEFT.asArg()))
                .addAction(new Pause(FINGER, ofMillis(2)))
                .addAction(FINGER.createPointerUp(LEFT.asArg()));
        androidDriver.perform(Arrays.asList(tap));
    }


}
