package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Store the text in alert into a runtime variable variable-name",
        description = "Store the text present in alert into a runtime variable",
        applicationType = ApplicationType.WEB)
public class ExtractTextFromAlert extends WebAction {

  @TestData(reference = "variable-name",isRuntimeVariable = true)
  private com.testsigma.sdk.TestData testData;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    String currentWindowHandle = driver.getWindowHandle();
    try{
      Alert alert = driver.switchTo().alert();
      logger.info("Alert is present in the screen");

      String alertText = alert.getText();
      logger.info("Text in the alert is: "+alertText);

      runTimeData.setKey(testData.getValue().toString());
      runTimeData.setValue(alertText);
      logger.info("Stored the alert text in runtime data");

      setSuccessMessage("Successfully extracted text in alert and stored it in a runtime variable "+testData.getValue().toString());
    } catch (NoAlertPresentException e){
      logger.info(ExceptionUtils.getStackTrace(e));
      setErrorMessage("There is no alert present in the screen");
      result = Result.FAILED;
    } catch (Exception e){
      logger.info(ExceptionUtils.getStackTrace(e));
      setErrorMessage("Unable to extract data from the alert");
      result = Result.FAILED;
    } finally {
      driver.switchTo().window(currentWindowHandle);
    }
    return result;
  }
}