package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Split string testdata using space and store the position into a variable",
        description = "validates options count in a select drop-down",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class MyFirstWebAction extends WebAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData;
  @TestData(reference = "position")
  private com.testsigma.sdk.TestData count;

  @TestData(reference = "variable", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData var;


  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String string = "";
    String removepostprespaces = testData.getValue().toString().trim();
    logger.info("after trim space post and pre"+removepostprespaces);
    String[] test = removepostprespaces.split(" ");

    int out = Integer.parseInt(count.getValue().toString());
    logger.info("index text"+test[out]);
    if (out >= 0 && out < test.length) { // Checking array bounds
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
      setSuccessMessage("Executed successfully");


    } catch (AssertionError error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn(error.toString());
      setErrorMessage("Failed");
    }

    return result;
  }
}