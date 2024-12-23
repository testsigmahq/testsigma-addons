package com.carrier.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.Result;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Split text from range of characters from and to and store extracted text in runtime-variable",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)

public class SplitStringInRangeAction extends AndroidAction {

  @TestData(reference = "text")
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "from")
  private com.testsigma.sdk.TestData from;

  @TestData(reference = "to")
  private com.testsigma.sdk.TestData to;

  @TestData(reference = "runtime-variable")
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData extractedTextRunTimeVariable;

  @Override
  protected Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    logger.info("Initiating execution");
    try {
      String text = testData.getValue().toString();
      int fromIndex = Integer.parseInt(from.getValue().toString());
      int toIndex = Integer.parseInt(to.getValue().toString());

      if (fromIndex < 0 || toIndex > text.length() || fromIndex > toIndex) {
        throw new IllegalArgumentException("Invalid range specified");
      }

      String extractedText = text.substring(fromIndex, toIndex);
      extractedTextRunTimeVariable = new com.testsigma.sdk.RunTimeData();
      extractedTextRunTimeVariable.setValue(extractedText);
      extractedTextRunTimeVariable.setKey(variableName.getValue().toString());

      logger.info("Extracted text: " + extractedText);

    } catch (Exception e) {
      result = Result.FAILED;
      String errorTrace = ExceptionUtils.getStackTrace(e);
      setErrorMessage("Error while executing action: " + errorTrace);
    }
    return result;
  }
}
