package com.testsigma.addons.restapi;

import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Data
@Action(actionText = "Generate SHA256 hash for input input-string and store it in runtime variable variable-name",
        description = "Generates SHA-256 hash for given input text and stores the hashed value in runtime variable",
        applicationType = ApplicationType.REST_API,
        useCustomScreenshot = false
)
public class GenerateSHA256Hash extends RestApiAction {

  @TestData(reference = "input-string")
  private com.testsigma.sdk.TestData inputString;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution....");
    try {
      String originalText = inputString.getValue().toString();
      logger.info("Original text is: " + originalText);

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(originalText.getBytes(StandardCharsets.UTF_8));

      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }

      logger.info("SHA-256 Hashed value is: " + hexString.toString());

      runTimeData.setValue(hexString.toString());
      runTimeData.setKey(variableName.getValue().toString());

      setSuccessMessage("SHA-256 hash generated and stored successfully." + variableName.getValue().toString() + " = " + hexString.toString());
    } catch (Exception e) {
      logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
      result = com.testsigma.sdk.Result.FAILED;
    }
    return result;
  }
}