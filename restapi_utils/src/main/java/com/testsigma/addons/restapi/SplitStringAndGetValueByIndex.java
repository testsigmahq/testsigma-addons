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
    actionText = "Split the input string test-data using regex pattern regex-pattern and store value at index index-number in runtime variable variable-name",
    description = "Splits the input string using the specified regex pattern and stores the value at the given index in the runtime variable.",
    applicationType = ApplicationType.REST_API,
    useCustomScreenshot = false
)
public class SplitStringAndGetValueByIndex extends RestApiAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "regex-pattern")
  private com.testsigma.sdk.TestData regexPattern;

  @TestData(reference = "index-number")
  private com.testsigma.sdk.TestData indexNumber;

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
      int index = Integer.parseInt(indexNumber.getValue().toString());
      String[] parts = input.split(regex);

      if (index < 0 || index >= parts.length) {
          result = com.testsigma.sdk.Result.FAILED;
          logger.warn("Index out of bounds: " + index);
          setErrorMessage("Index " + index + " is out of range. Total parts: " + parts.length);
          return result;
      }

      String value = parts[index].trim();
      logger.info("Value is: " + value);

      runTimeData.setKey(variableName.getValue().toString());
      runTimeData.setValue(value);

      setSuccessMessage("Successfully retrieved value '" + value + "' at index " + index +
          " after splitting string using regex '" + regex + "'. Stored in runtime variable: " + variableName.getValue());
    } catch (Exception e) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to get value by index: " + ExceptionUtils.getMessage(e));
    }
    return result;
  }
}
