package com.testsigma.addons.generators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import java.util.Base64;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Decode the string test-data to base64",
        description = "Decodes the string test-data to base64")
public class DecodeStringToBase64 extends TestDataFunction {

  @TestDataFunctionParameter (reference = "string")
  private com.testsigma.sdk.TestDataParameter string;

  @Override
  public TestData generate() throws Exception {
    // Try use of run time data
    logger.info("Initiating execution");
    String decodedString = new String(Base64.getDecoder().decode(string.getValue().toString()));
    TestData testData = new TestData(decodedString);
    return testData;
  }
}