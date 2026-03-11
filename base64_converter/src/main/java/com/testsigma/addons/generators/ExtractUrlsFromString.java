package com.testsigma.addons.generators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Extract matching values from the input string using regex",
        description = "Extracts all values matching the given regex from the input string and returns them comma-separated")
public class ExtractUrlsFromString extends TestDataFunction {

  @TestDataFunctionParameter(reference = "string")
  private com.testsigma.sdk.TestDataParameter string;

  @TestDataFunctionParameter(reference = "regex")
  private com.testsigma.sdk.TestDataParameter regex;

  @Override
  public TestData generate() throws Exception {
    logger.info("Initiating execution");
    String input = string.getValue().toString();
    Pattern pattern = Pattern.compile(regex.getValue().toString());
    Matcher matcher = pattern.matcher(input);
    List<String> matches = new ArrayList<>();
    while (matcher.find()) {
      matches.add(matcher.group());
    }
    if (matches.isEmpty()) {
      throw new Exception("No matches found for the given regex in the provided string");
    }
    String result = String.join(",", matches);
    return new TestData(result);
  }
}
