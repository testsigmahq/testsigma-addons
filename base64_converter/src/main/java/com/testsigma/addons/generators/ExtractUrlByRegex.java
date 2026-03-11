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
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Extract URLs matching regex from the input string",
        description = "Extracts all URLs from the input string that match the given regex and returns them comma-separated")
public class ExtractUrlByRegex extends TestDataFunction {

  private static final Pattern URL_PATTERN = Pattern.compile(
          "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);

  @TestDataFunctionParameter(reference = "string")
  private com.testsigma.sdk.TestDataParameter string;

  @TestDataFunctionParameter(reference = "regex")
  private com.testsigma.sdk.TestDataParameter regex;

  @Override
  public TestData generate() throws Exception {
    logger.info("Initiating execution");
    String input = string.getValue().toString();
    Pattern userPattern = Pattern.compile(regex.getValue().toString());

    List<String> matchingUrls = new ArrayList<>();
    Matcher urlMatcher = URL_PATTERN.matcher(input);
    while (urlMatcher.find()) {
      String url = urlMatcher.group(1);
      if (userPattern.matcher(url).find()) {
        matchingUrls.add(url);
      }
    }
    if (matchingUrls.isEmpty()) {
      throw new Exception("No URLs matching the given regex were found in the provided string");
    }
    String result = String.join(",", matchingUrls);
    return new TestData(result);
  }
}
