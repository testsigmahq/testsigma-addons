package com.testsigma.addons.generators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import java.util.Base64;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Decode the input string to base64",
        description = "Decodes the input string to base64")
public class DecodeStringToBase64 extends TestDataFunction {

  @TestDataFunctionParameter (reference = "string")
  private com.testsigma.sdk.TestDataParameter string;

  @Override
  public TestData generate() throws Exception {
    logger.info("Initiating execution");
    String input = string.getValue().toString();
    String decodedString;
    boolean hasUrlSafeChars = input.contains("-") || input.contains("_");
    boolean hasStandardChars = input.contains("+") || input.contains("/");

    if (hasUrlSafeChars && !hasStandardChars) {
      logger.info("Detected URL-safe Base64 encoding, using URL decoder");
      decodedString = new String(Base64.getUrlDecoder().decode(input));
    } else {
      try {
        decodedString = new String(Base64.getDecoder().decode(input));
      } catch (IllegalArgumentException ex) {
        logger.info("Standard Base64 decoding failed, falling back to URL-safe decoder");
        decodedString = new String(Base64.getUrlDecoder().decode(input));
      }
    }

    TestData testData = new TestData(decodedString);
    return testData;
  }
}