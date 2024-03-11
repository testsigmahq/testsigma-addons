package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

@Data
@Action(actionText = "Right click on text name",
        description = "Right click on the text using the text coordinates",
        applicationType = ApplicationType.WEB)

public class RightClickOnText extends WebAction {
    @OCR
    private com.testsigma.sdk.OCR ocr;

    @TestData(reference = "name")
    private com.testsigma.sdk.TestData text;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        List<OCRTextPoint> textPoints = ocr.extractTextFromPage();
        printAllCoordinates(textPoints);
        OCRTextPoint textPoint = getTextPointFromText(textPoints);
        if(textPoint == null) {
            result = Result.FAILED;
            setErrorMessage("Given text is not found");

        } else {
            logger.info("Found Textpoint with text = " + textPoint.getText() +  ", x1 = " + textPoint.getX1() +
                    ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
            clickOnCoordinates(textPoint);
            setSuccessMessage("Click operation performed on the text " +
                    "    Text coordinates :" + "x1-" + textPoint.getX1() + ", x2-" + textPoint.getX2() + ", y1-" + textPoint.getY1() + ", y2-" + textPoint.getY2());
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
    public void clickOnCoordinates(OCRTextPoint textPoint) {

        int x1 = textPoint.getX1();
        int y1 = textPoint.getY1();
        int x2 = textPoint.getX2();
        int y2 = textPoint.getY2();

//             int x = (x1 + x2) / 2;
//             int y = (y1 + y2) / 2;
        int x = x2;
        int y = y2;

        logger.info("MEAN X coordinate: " + x + "\n");
        logger.info("MEAN Y coordinate: " + y + "\n");

        Actions actions = new Actions(driver);
        actions.moveToLocation(x, y).contextClick().build().perform();
    }


}
