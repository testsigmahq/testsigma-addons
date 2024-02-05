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
@Action(actionText = "Verify that the text text-value is present on the screen",
        description = "Verify whether the text is present or not",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)

public class VerifyTextOnImage extends WindowsAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "text-value")
    private com.testsigma.sdk.TestData text;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        String inputText = text.getValue().toString().replaceAll("\\s+", "");
        try{
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
            OCRImage ocrImage = new OCRImage();
            ocrImage.setOcrImageFile(baseImageFile);

            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
            printAllCoordinates(textPoints);
            OCRTextPoint textPoint = getTextPointFromText(textPoints,inputText);
            if(textPoint == null) {
                result = Result.FAILED;
                setErrorMessage("Given text is not found");

            } else {
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
            }
        } catch (Exception e){
            logger.info("Exception: "+ Arrays.toString(e.getStackTrace()));
            setErrorMessage("Exception occurred while searching for the given text");
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
