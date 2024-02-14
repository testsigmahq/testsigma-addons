package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "Verify if the text test-data matches the pattern regex-pattern",
        description = "validates options count in a select drop-down",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class RegExPatternMatch extends WebAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "regex-pattern")
  private com.testsigma.sdk.TestData regExPattern;


  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    logger.info("Test Data::"+testData.getValue());
    logger.info("RegEx Pattern::"+regExPattern.getValue());

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    try {
      Pattern pattern = Pattern.compile(regExPattern.getValue().toString());
      Matcher matcher = pattern.matcher(testData.getValue().toString());
      if (matcher.matches()) {
        setSuccessMessage("The input string matches the pattern.");
        return Result.SUCCESS;
      } else {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("The input string does not match the pattern.");
        return result;
      }
    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info(" Exception occurred while executing the action::"+ ExceptionUtils.getStackTrace(error));
      setErrorMessage("Exception occurred while executing the action::"+error.getMessage());
      return result;
    }

  }
/*  public static void main(String... a){
    MyFirstWebAction myFirstWebAction = new MyFirstWebAction();
    myFirstWebAction.setTestData(new com.testsigma.sdk.TestData("Showing 5 most recent authorizations"));
    myFirstWebAction.setRegExPattern(new com.testsigma.sdk.TestData("Showing (\\d{1,2}|100) most recent authorizations"));
    myFirstWebAction.execute();
  }*/
}