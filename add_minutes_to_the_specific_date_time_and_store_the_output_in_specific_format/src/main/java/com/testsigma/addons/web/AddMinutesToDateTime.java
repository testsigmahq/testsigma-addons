package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@Action(actionText = "Testing Add minute minutes to the input-datetime with input-datetime-format, convert to output-datetime-format format, and store it in a runtime variable variable-name",
        description = "Adds specified minutes to a given input datetime, converts the resulting datetime to a specified date-time format, and stores it in a runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class AddMinutesToDateTime extends WebAction {

  @TestData(reference = "input-datetime")
  private com.testsigma.sdk.TestData dateTime;
  @TestData(reference = "input-datetime-format")
  private com.testsigma.sdk.TestData inputFormat;
  @TestData(reference = "output-datetime-format")
  private com.testsigma.sdk.TestData outputFormat;
  @TestData(reference = "minute")
  private com.testsigma.sdk.TestData minutesToAdd;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String dateTimeString = dateTime.getValue().toString();
    String inputFormatString = inputFormat.getValue().toString();
    String outputFormatString = outputFormat.getValue().toString();
    String minutesToAddString = minutesToAdd.getValue().toString();
    String variableNameString = variableName.getValue().toString();

    try {
      int minutes = Integer.parseInt(minutesToAddString);

      if (minutes < 0) {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("The 'minute' value must be a non-negative integer.");
        logger.warn("Invalid input: 'minute' value is negative.");
        return result;
      }

      String newDateTime = addMinutesToDateTime(dateTimeString, inputFormatString, outputFormatString, minutes);

      logger.info("New date-time value: " + newDateTime);

      if (newDateTime != null) {
        runTimeData.setValue(newDateTime);
        runTimeData.setKey(variableNameString);
        logger.info("Final value stored in variable '" + variableNameString + "' with value '" + newDateTime + "'");
        setSuccessMessage(String.format("Added %s minutes to %s and stored the output in '%s' format. %s : %s",
                minutes, dateTimeString, outputFormatString, variableNameString, newDateTime));
      } else {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("Failed to add minutes.  See logs for details.");
      }

    } catch (NumberFormatException e) {
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("Invalid number format for 'minutes': " + minutesToAddString);
      logger.warn("NumberFormatException: " + e);

    } catch (Exception e) {
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("An unexpected error occurred: " + e.getMessage());
      logger.warn("Exception during execution: " + e);
    }
    return result;
  }

  public String addMinutesToDateTime(String dateTimeString, String inputFormatString, String outputFormatString, int minutesToAdd) {
    try {
      DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormatString);
      DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormatString);
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);

      LocalDateTime newDateTime = dateTime.plusMinutes(minutesToAdd);
      logger.info("Successfully added minutes to the input datetime");

      return newDateTime.format(outputFormatter);

    } catch (DateTimeParseException e) {
      String errorMessage = "Invalid date/time format. Please use the format: " + inputFormatString + ". Error: " + e.getMessage();
      logger.warn(errorMessage);
      setErrorMessage(errorMessage);
      return null;
    }
  }
}