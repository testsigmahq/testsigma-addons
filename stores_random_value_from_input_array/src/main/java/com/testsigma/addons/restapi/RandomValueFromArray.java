package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.util.concurrent.ThreadLocalRandom;

@Data
@Action(
        actionText = "Select random value from input-array and store in runtime variable variable-name",
        description = "Accepts a string array input like [\"dt1\",\"dt2\",\"dt3\"], selects a random value, and stores it in runtime data",
        applicationType = ApplicationType.REST_API,
        useCustomScreenshot = false
)
public class RandomValueFromArray extends RestApiAction {

  @TestData(reference = "input-array")
  private com.testsigma.sdk.TestData inputArray;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() {

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");

    try {

      String input = inputArray.getValue().toString().trim();

      // Remove [ and ]
      String cleaned = input.substring(1, input.length() - 1);

      // Remove quotes
      cleaned = cleaned.replace("\"", "");

      // Convert to array
      String[] values = cleaned.split(",");

      // Pick random value
      int randomIndex = ThreadLocalRandom.current().nextInt(values.length);
      String randomValue = values[randomIndex].trim();

      runTimeData = new com.testsigma.sdk.RunTimeData();
      runTimeData.setKey(variableName.getValue().toString());
      runTimeData.setValue(randomValue);

      setSuccessMessage("Random value selected and stored: " + randomValue);

    } catch (Exception e) {
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("Failed to select random value from input array");
    }

    return result;
  }
}
