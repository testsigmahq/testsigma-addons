package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Action(
    actionText = "Split the input string test-data using regex pattern regex-pattern and store split count in runtime variable variable-name",
    description = "Splits the given string using the provided regex pattern and stores the total number of parts in the specified runtime variable.",
    applicationType = ApplicationType.REST_API,
    useCustomScreenshot = false
)
public class SplitStringAndGetCount extends RestApiAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "regex-pattern")
  private com.testsigma.sdk.TestData regexPattern;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
      String input = testData.getValue().toString();
      String regex = regexPattern.getValue().toString();
      String[] parts = input.split(regex);
      int count = parts.length;
      logger.info("Length of parts: " + count);

      runTimeData.setKey(variableName.getValue().toString());
      runTimeData.setValue(String.valueOf(count));

      setSuccessMessage("Successfully split the string using regex '" + regex +
          "'. Count of parts: " + count + ". Stored in runtime variable: " + variableName.getValue());
    } catch (Exception e) {;
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to split string: " + ExceptionUtils.getMessage(e));
    }
    return result;
  }
}
