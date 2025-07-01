package com.gmail.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Dimension;
import lombok.Data;

@Data
@Action(actionText = "Set screen resolution to test-data1 and test-data2",
        description = "Set screen resolution",
        applicationType = ApplicationType.WEB)
public class ScreenResolutionWebAction extends WebAction {

  @TestData(reference = "test-data1")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "test-data2")
  private com.testsigma.sdk.TestData testData2;


  @Override
  public com.testsigma.sdk.Result execute() {
    // Try use of run time data
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Starting screen resolution update...");

    try {
      int width = Integer.parseInt(testData1.getValue().toString().trim());
      int height = Integer.parseInt(testData2.getValue().toString().trim());
      driver.manage().window().setSize(new Dimension(width, height));
      logger.info("Screen resolution set to" + width + " x " + height);
      setSuccessMessage("Screen resolution set to" + width + " x " + height);
    } catch (Exception e) {
      setErrorMessage("Exception: " + ExceptionUtils.getStackTrace(e));
      result = com.testsigma.sdk.Result.FAILED;
    }

    return result;
  }
}