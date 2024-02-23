package com.testsigma.addons.windows;

import com.testsigma.sdk.*;
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
import java.util.List;

@Data
@Action(actionText = "Wait for test-data seconds until the text text-value is present on screen",
        description = "Wait until the given text is present on screen",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)

public class WaitUntilTextIsPresent extends WindowsAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData time;

    @TestData(reference = "text-value")
    private com.testsigma.sdk.TestData text;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        String errorMessage = "Exception occurred while finding the text point";
        String inputText = text.getValue().toString().replaceAll("\\s+", "");
        try{
            Robot robot = new Robot();
            long currentTimeMillis = System.currentTimeMillis();
            int givenTimeout = Integer.parseInt(time.getValue().toString());
            long endTimeMillis = currentTimeMillis + (givenTimeout * 1000L);
            int pollInterval = 5000;
            OCRTextPoint textPoint = null;
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
                OCRImage ocrImage = new OCRImage();
                ocrImage.setOcrImageFile(baseImageFile);

                List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
                textPoint = getTextPointFromText(textPoints,inputText);
                if(textPoint!=null){
                    printAllCoordinates(textPoints);
                    logger.info("Found Textpoint with text = " + textPoint.getText() +  ", x1 = " + textPoint.getX1() +
                            ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
                    tmp = robot.createScreenCapture(screenSize);
                    filename = "screenshot"+System.currentTimeMillis()+".jpg";
                    path = tempDir + filename;
                    ImageIO.write(tmp, "jpg",new File(path));
                    baseImageFile = new File(path);
                    String url = testStepResult.getScreenshotUrl();
                    ocr.uploadFile(url, baseImageFile);
                    setSuccessMessage("Text is found at " +
                            "    Text coordinates :" + "x1-" + textPoint.getX1() + ", x2-" + textPoint.getX2() + ", y1-" + textPoint.getY1() + ", y2-" + textPoint.getY2());
                    break;
                } else{
                    Thread.sleep(pollInterval);
                }
                currentTimeMillis = System.currentTimeMillis();
            }
            if(textPoint == null){
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
    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints,String text) {
        if(textPoints == null) {
            return null;
        }
        for(OCRTextPoint textPoint: textPoints) {
            if(text.contains(textPoint.getText())) {
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

}
