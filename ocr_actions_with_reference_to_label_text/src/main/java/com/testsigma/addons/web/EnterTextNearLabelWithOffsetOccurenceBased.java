package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;


@Data
@Action(actionText = "Enter testdata on element with label text label-text at occurence label-occurence where the element position is positions and the offset value offset-x, offset-y",
        description = "Enters text into an element near a label found using OCR, based on relative position",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)

public class EnterTextNearLabelWithOffsetOccurenceBased extends WindowsAction {

    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData inputData; // Text to enter

    @TestData(reference = "label-text")
    private com.testsigma.sdk.TestData labeltext; // Label text

    @TestData(reference = "label-occurence")
    private com.testsigma.sdk.TestData labelOccurence; // Label occurence

    @TestData(reference = "positions", allowedValues = {"Below Given Text", "Above Given Text", "Before Given Text", "After Given Text"})
    private com.testsigma.sdk.TestData position;

    @TestData(reference = "offset-x")
    private com.testsigma.sdk.TestData offsetX;

    @TestData(reference = "offset-y")
    private com.testsigma.sdk.TestData offsetY;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        int X_OFFSET = Integer.parseInt(offsetX.getValue().toString());
        int Y_OFFSET = Integer.parseInt(offsetY.getValue().toString());
        try {
            Robot robot = new Robot();

            // Fetch the Details of the Screen Size
            Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // Take the Snapshot of the Screen
            BufferedImage tmp = robot.createScreenCapture(screenSize);

            // Provide the destination details to copy the screenshot
            String tempDir = System.getProperty("java.io.tmpdir");
            String filename = "screenshot" + System.currentTimeMillis() + ".jpg";
            String path = tempDir + filename;

            // To copy source image in to destination path
            ImageIO.write(tmp, "jpg", new File(path));
            int width = tmp.getWidth();
            int height = tmp.getHeight();
            logger.info("Width of image: " + width);
            logger.info("Height of image: " + height);

            File baseImageFile = new File(path);
            OCRImage ocrImage = new OCRImage();
            ocrImage.setOcrImageFile(baseImageFile);

            List<OCRTextPoint> textPoints = ocr.extractTextFromImage(ocrImage);
            printAllCoordinates(textPoints);
            int targetOccurrence = Integer.parseInt(labelOccurence.getValue().toString());
            OCRTextPoint textPoint = getTextPointFromText(textPoints, targetOccurrence);
            if (textPoint == null) {
                result = Result.FAILED;
                setErrorMessage("Given text is not found at the specified occurrence.");

            } else {
                logger.info("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                        ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());

                logger.info("position: " + position.getValue().toString());
                Point elementLocation = calculateElementLocation(textPoint, position.getValue().toString(), X_OFFSET, Y_OFFSET);
                logger.info("Element location = " + elementLocation);

                // Click on the calculated location
                robot.mouseMove(elementLocation.x, elementLocation.y);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                robot.delay(100); //Small delay after click

                // Paste the text into the element
                String textToEnter = inputData.getValue().toString();  // Convert to String
                StringSelection stringSelection = new StringSelection(textToEnter);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
                robot.keyPress(java.awt.event.KeyEvent.VK_V);
                robot.keyRelease(java.awt.event.KeyEvent.VK_V);
                robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
                robot.delay(100); //Small delay after paste


                tmp = robot.createScreenCapture(screenSize);
                filename = "screenshot" + System.currentTimeMillis() + ".jpg";
                path = tempDir + filename;
                ImageIO.write(tmp, "jpg", new File(path));
                baseImageFile = new File(path);
                String url = testStepResult.getScreenshotUrl();
                ocr.uploadFile(url, baseImageFile);
                setSuccessMessage("Entered text '" + inputData.getValue() + "' on element near label '" + labeltext.getValue() +
                        "' at position: " + position.getValue().toString());
            }
        } catch (Exception e) {
            logger.info("Exception: " + Arrays.toString(e.getStackTrace()));
            setErrorMessage("Exception occurred: " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }

    private OCRTextPoint getTextPointFromText(List<OCRTextPoint> textPoints, int targetOccurrence) {
        if (textPoints == null) {
            return null;
        }
        int occurrences = 0;
        for (OCRTextPoint textPoint : textPoints) {
            if (textPoint.getText().contains(labeltext.getValue().toString())) {
                occurrences += 1;
                if (occurrences == targetOccurrence) {
                    return textPoint;
                }
            }
        }
        return null;
    }

    private void printAllCoordinates(List<OCRTextPoint> textPoints) {
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("text =" + textPoint.getText() + "x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() + "\n\n\n\n");
        }
    }

    int x, y;
    private Point calculateElementLocation(OCRTextPoint textPoint, String position, int X_OFFSET, int Y_OFFSET) {
        int x1 = textPoint.getX1();
        int y1 = textPoint.getY1();
        int x2 = textPoint.getX2();
        int y2 = textPoint.getY2();

        switch (position.toLowerCase()) {
            case "below given text":
                logger.info("below given text position");
                x = (x1 + x2) / 2;
                y = y2 + Y_OFFSET;
                break;
            case "above given text":
                logger.info("above given text position");
                x = (x1 + x2) / 2;
                y = y1 - Y_OFFSET;
                break;
            case "before given text":
                logger.info("before given text position");
                x = x1 - X_OFFSET;
                y = (y1 + y2) / 2;
                break;
            case "after given text":
                logger.info("after given text position");
                x = x2 + X_OFFSET;
                y = (y1 + y2) / 2;
                break;
            default:
                logger.info("Invalid position specified.  Clicking after the text.");
                break;
        }
        logger.info("Final Coordinates " + "x = " + x + ", y = " + y);

        return new Point(x, y);
    }
}