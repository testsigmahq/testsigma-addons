package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.OCRTextPoint;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;

@Data
@Action(actionText = "Verify that the current page does not display text testdata",
        description = "Verifies that the current page does not display text",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = true)
public class VerifyTextNotPresentOnCurrentPage extends IOSAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData text;

  @OCR
  private com.testsigma.sdk.OCR ocr;

  @TestStepResult
  private com.testsigma.sdk.TestStepResult testStepResult;

  @Override
  protected Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    Result result = Result.SUCCESS;
    try {
      List<OCRTextPoint> textPoints = ocr.extractTextFromPage();
      printAllCoordinates(textPoints);
      OCRTextPoint textPoint = getTextPointFromText(textPoints);
      if (textPoint == null) {
        logger.info("Given text not found in the current page");
        setSuccessMessage("Given text not found in the current page");
      } else {
        logger.warn("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());
        setErrorMessage("Found Textpoint with text = " + textPoint.getText() + ", x1 = " + textPoint.getX1() +
                ", y1 = " + textPoint.getY1() + ", x2 = " + textPoint.getX2() + ", y2 = " + textPoint.getY2());

        result = Result.FAILED;
      }

    } catch (Exception e) {
      logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception: " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }

    return result;
  }

  private void printAllCoordinates(List<OCRTextPoint> textPoints) {
    for(OCRTextPoint textPoint: textPoints) {
      logger.info("text =" + textPoint.getText() + "x1 = " + textPoint.getX1() + ", y1 =" + textPoint.getY1()  + ", x2 = " + textPoint.getX2() + ", y2 =" + textPoint.getY2() +"\n\n\n\n");
    }
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
}