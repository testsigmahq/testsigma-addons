package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Action(
        actionText = "Convert date values-date and time in format hh:mm:ss AM/PM values-time to epoch time using system time zone and store it in a runtime variable variable-name",
        description = "Converts the given date and 12-hour time format (hh:mm:ss AM/PM) to epoch time using the system's default time zone, and stores the result in a runtime variable",
        applicationType = ApplicationType.IOS
)
public class EpochTimeConverterWithSystemZone extends IOSAction {

  @TestData(reference = "values-date")
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "values-time")
  private com.testsigma.sdk.TestData testData2;

  @TestData(reference = "variable-name")
  private com.testsigma.sdk.TestData testData3;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");
    logger.debug("Date Value: " + testData.getValue().toString() + " Time Value: " + testData2.getValue().toString());

    String inputDate = String.valueOf(this.testData.getValue());
    String inputTime = String.valueOf(this.testData2.getValue());

    long epochTime = convertToEpochTime(inputDate, inputTime);

    runTimeData = new com.testsigma.sdk.RunTimeData();
    runTimeData.setKey(testData3.getValue().toString());
    runTimeData.setValue(String.valueOf(epochTime));

    logger.debug("Epoch Time: " + epochTime);
    setSuccessMessage("Epoch time successfully converted and stored in '" + testData3.getValue() + "': " + epochTime);

    return com.testsigma.sdk.Result.SUCCESS;
  }

  private static long convertToEpochTime(String date, String time) {
    String dateTimeString = date + " " + time;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
    return dateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
  }
}
