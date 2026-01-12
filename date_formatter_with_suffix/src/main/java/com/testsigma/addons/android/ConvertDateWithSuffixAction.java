package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Action(
        actionText = "Convert date date-val of format input-format to format options and store it in runtime variable variable-name",
        description = "Converts date from any input format to output format with suffix support",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false
)
public class ConvertDateWithSuffixAction extends AndroidAction {

  @TestData(reference = "date-val")
  private com.testsigma.sdk.TestData dateVal;

  @TestData(reference = "input-format")
  private com.testsigma.sdk.TestData inputFormat;

  @TestData(
          reference = "options",
          allowedValues = {
                  "d<suffix> MMM, yyyy",
                  "d<suffix> MMMM, yyyy",
                  "dd<suffix> MM yyyy",
                  "MMMM d<suffix>, yyyy",
                  "EEEE, d<suffix> MMMM yyyy"
          }
  )
  private com.testsigma.sdk.TestData options;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {

    logger.info("Initiating execution");

    try {
      String inputDate = dateVal.getValue().toString();
      String inputPattern = inputFormat.getValue().toString();
      String outputPattern = options.getValue().toString();

      logger.info("Input date value : " + inputDate);
      logger.info("Input format     : " + inputPattern);
      logger.info("Output format    : " + outputPattern);

      // Parse input date using provided format
      DateTimeFormatter inputFormatter =
              DateTimeFormatter.ofPattern(inputPattern);

      LocalDate date = LocalDate.parse(inputDate, inputFormatter);

      // Format with suffix
      String formattedDate = formatWithSuffix(date, outputPattern);

      // Store runtime variable
      runTimeData.setValue(formattedDate);
      runTimeData.setKey(variableName.getValue().toString());

      setSuccessMessage(
              "Date '" + inputDate + "' converted successfully to '" +
                      formattedDate + "' and stored in runtime variable '" +
                      variableName.getValue() + "'"
      );

      return Result.SUCCESS;

    } catch (Exception e) {
      logger.warn("Exception Occurred: " + e.getMessage());
      setErrorMessage("Date conversion failed: " + e.getMessage());
      return Result.FAILED;
    }
  }

  private static String formatWithSuffix(LocalDate date, String format) {

    int day = date.getDayOfMonth();
    String suffix = getDaySuffix(day);

    String resolvedFormat = format.contains("<suffix>")
            ? format.replace("<suffix>", "'" + suffix + "'")
            : format;

    return date.format(DateTimeFormatter.ofPattern(resolvedFormat));
  }

  private static String getDaySuffix(int day) {
    if (day >= 11 && day <= 13) return "th";
    switch (day % 10) {
      case 1: return "st";
      case 2: return "nd";
      case 3: return "rd";
      default: return "th";
    }
  }
}
