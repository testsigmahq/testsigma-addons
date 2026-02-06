package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Data
@Action(
  actionText = "Split string inputString using delimiter delimiter-value and get value at index index-value and store it in runtime variable variable-name",
  description = "Splits a string using a delimiter and stores the value at the given index in a runtime variable",
  applicationType = ApplicationType.REST_API
)
public class SplitStringUsingDelimiter extends RestApiAction {

  @TestData(reference = "inputString")
  private com.testsigma.sdk.TestData inputString;

  @TestData(reference = "delimiter-value")
  private com.testsigma.sdk.TestData delimiter;

  @TestData(reference = "index-value")
  private com.testsigma.sdk.TestData index;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  protected Result execute() throws NoSuchElementException {
    logger.info("Initiating execution.....");
    Result result = Result.SUCCESS;
    try {
      String input = inputString.getValue().toString();
      String delim = delimiter.getValue().toString();
      int idx = Integer.parseInt(index.getValue().toString());
      String runtimeVar = variableName.getValue().toString();

      logger.info("Input string: " + input);
      logger.info("Delimiter: " + delim);
      logger.info("Index: " + idx);

      String[] parts = input.split(Pattern.quote(delim));

      if (idx < 0 || idx >= parts.length) {
        logger.warn("Index out of bounds: " + idx);
        setErrorMessage("Index " + idx + " is out of bounds. Split size: " + parts.length);
        return Result.FAILED;
      }

      String value = parts[idx];
      logger.info("Value: " + value);

      runTimeData.setKey(runtimeVar);
      runTimeData.setValue(value);


      setSuccessMessage(
        "Successfully stored value '" + value + "' in runtime variable '" + runtimeVar + "'"
      );
    } catch (Exception e) {
      logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception: " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }

    return result;
  }
}