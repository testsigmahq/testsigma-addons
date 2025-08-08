package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Split string testdata using character and store the position into a variable",
        description = "validates options count in a select drop-down",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SplitStringUsingCharacterAction extends WebAction {

  @TestData(reference = "character")
  private com.testsigma.sdk.TestData character ;

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData;
  @TestData(reference = "position")
  private com.testsigma.sdk.TestData count;

  @TestData(reference = "variable")
  private com.testsigma.sdk.TestData var;


  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String string = "";
    String splitCharacter = character.getValue().toString();
    String regexSafeCharacter = java.util.regex.Pattern.quote(splitCharacter);
    String[] test = testData.getValue().toString().split(regexSafeCharacter);
    System.out.println(test[1]);

    int out = Integer.parseInt(count.getValue().toString());
    if (out >= 0 && out < test.length) {
      string = test[out];
      logger.debug("Out: " + string);
      logger.info("String: " + string);
    } else {

      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Index out of bounds or invalid input");
      setErrorMessage("Index out of bounds or invalid input");
    }

    try {

      runTimeData = new com.testsigma.sdk.RunTimeData();
      runTimeData.setKey(var.getValue().toString());
      runTimeData.setValue(string);


    } catch (AssertionError error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn(error.toString());
      setErrorMessage("Failed");
    }
    setSuccessMessage("Successfully splitted the string");
    return result;
  }
}