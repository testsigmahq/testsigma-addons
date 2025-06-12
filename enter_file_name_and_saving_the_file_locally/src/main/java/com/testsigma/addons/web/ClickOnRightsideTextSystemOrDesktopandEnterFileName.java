package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Data
@Action(actionText = "Click on right side text testdata,occurrence occurrence-position on system/desktop window and enter filename",
        description = "Click on the text using OCR and type the filename into the field to its right",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)
public class ClickOnRightsideTextSystemOrDesktopandEnterFileName extends WebAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData text;

    @TestData(reference = "occurrence-position")
    private com.testsigma.sdk.TestData occurrencePosition;

    @TestData(reference = "filename")
    private com.testsigma.sdk.TestData filename;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private double xrelative;
    private double yrelative;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        try {
            if (isCloudExecution()) {
                setErrorMessage("This action is not supported in cloud execution environments.");
                return Result.FAILED;
            }

            Robot robot = new Robot();
            Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage tmp = robot.createScreenCapture(screenSize);

            String tempDir = System.getProperty("java.io.tmpdir");
            String filenameImg = "screenshot" + System.currentTimeMillis() + ".jpg";
            String path = tempDir + filenameImg;
            ImageIO.write(tmp, "jpg", new File(path));

            int width = tmp.getWidth();
            int height = tmp.getHeight();

            File baseImageFile = new File(path);
            OCRImage ocrImage = new OCRImage();
            ocrImage.setOcrImageFile(baseImageFile);

            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);

            int target_occurrence = Integer.parseInt(occurrencePosition.getValue().toString());
            printAllCoordinates(textPoints);
            OCRTextPoint textPoint = getTextPointFromText(textPoints, target_occurrence);

            if (textPoint == null) {
                setErrorMessage("Given text is not found");
                return Result.FAILED;
            }

            logger.info("Found Textpoint with text = " + textPoint.getText() +
                    ", x1 = " + textPoint.getX1() +
                    ", y1 = " + textPoint.getY1() +
                    ", x2 = " + textPoint.getX2() +
                    ", y2 = " + textPoint.getY2());

            clickOnCoordinates(textPoint, width, height);

            // Take screenshot after click
            tmp = robot.createScreenCapture(screenSize);
            filenameImg = "screenshot" + System.currentTimeMillis() + ".jpg";
            path = tempDir + filenameImg;
            ImageIO.write(tmp, "jpg", new File(path));
            baseImageFile = new File(path);
            String url = testStepResult.getScreenshotUrl();
            ocr.uploadFile(url, baseImageFile);

            // Move right and click in filename input field
            int offset = 150; // Adjust if needed
            robot.mouseMove((int) xrelative + offset, (int) yrelative);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(500);

            // Type the filename
            String fileNameToType = filename.getValue().toString();
            logger.info("Filename to type: " + fileNameToType); // Debug log
            for (char ch : fileNameToType.toCharArray()) {
                boolean isUpperCase = Character.isUpperCase(ch);
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase(ch));
                if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                    logger.warn("Key code not found for character '" + ch + "'");
                    continue;
                }
                logger.info("Typing character: " + ch + ", Key code: " + keyCode + ", Is uppercase: " + isUpperCase);
                if (isUpperCase) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                }
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
                if (isUpperCase) {
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                }
                robot.delay(100);
            }

            // Take screenshot after typing
            tmp = robot.createScreenCapture(screenSize);
            String postTypeFilenameImg = "screenshot_after_type" + System.currentTimeMillis() + ".jpg";
            String postTypePath = tempDir + postTypeFilenameImg;
            ImageIO.write(tmp, "jpg", new File(postTypePath));
            logger.info("Screenshot after typing saved at: " + postTypePath);

            setSuccessMessage("Click operation performed on the text. Entered filename: " + fileNameToType);
        } catch (Exception e) {
            logger.info("Exception: " + Arrays.toString(e.getStackTrace()));
            setErrorMessage("Exception occurred while processing the text or filename input: " + e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }

    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints,int target_occurrence) {
        if(textPoints == null) {
            return null;
        }
        int occurrences = 0;
        for(OCRTextPoint textPoint: textPoints) {
            if(text.getValue().equals(textPoint.getText())) {
                occurrences+=1;
                if(occurrences == target_occurrence){
                    return textPoint;
                }
            }
        }
        return  null;
    }

    private void printAllCoordinates(List<OCRTextPoint> textPoints) {
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("text = " + textPoint.getText() +
                    ", x1 = " + textPoint.getX1() +
                    ", y1 = " + textPoint.getY1() +
                    ", x2 = " + textPoint.getX2() +
                    ", y2 = " + textPoint.getY2());
        }
    }

    public void clickOnCoordinates(OCRTextPoint textPoint, int imageWidth, int imageHeight) throws AWTException {
        Robot robot = new Robot();

        int x = (textPoint.getX1() + textPoint.getX2()) / 2;
        int y = (textPoint.getY1() + textPoint.getY2()) / 2;

        JavascriptExecutor js = (JavascriptExecutor) driver;
        long browserHeight = (Long) js.executeScript("return window.innerHeight;");
        long browserWidth = (Long) js.executeScript("return window.innerWidth;");

        xrelative = ((double) x / (double) browserWidth) * (double) imageWidth;
        yrelative = ((double) y / (double) browserHeight) * (double) imageHeight;

        logger.info("X relative: " + (int) xrelative);
        logger.info("Y relative: " + (int) yrelative);

        robot.mouseMove((int) xrelative, (int) yrelative);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private boolean isCloudExecution() {
        try {
            return !(driver instanceof ChromiumDriver || driver instanceof EdgeDriver ||
                    driver instanceof FirefoxDriver || driver instanceof SafariDriver);
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while checking for cloud execution: " + e.getMessage());
            return false;
        }
    }
}
