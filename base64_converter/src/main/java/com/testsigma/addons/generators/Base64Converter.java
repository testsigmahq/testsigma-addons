package com.testsigma.addons.generators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import java.util.Base64;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Convert the string test-data to base64",
        description = "Converts the string test-data to base64")
public class Base64Converter extends TestDataFunction {

  @TestDataFunctionParameter (reference = "string")
  private com.testsigma.sdk.TestDataParameter string;

  @Override
  public TestData generate() throws Exception {
    // Try use of run time data
    logger.info("Initiating execution");
    String base64String = Base64.getEncoder().encodeToString(string.getValue().toString().getBytes());
    TestData testData = new TestData(base64String);
    return testData;
  }
}