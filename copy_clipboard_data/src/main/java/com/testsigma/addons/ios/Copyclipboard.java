package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Copy data from the clipboard and store into a runtime variable testdata",
        description = "Copying the data from the clipboard and store into a runtime variable",
        applicationType = ApplicationType.IOS)
public class Copyclipboard extends IOSAction {

  @TestData(reference = "testdata", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData testData1;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
	  
	    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
	    logger.info("Initiating execution");

        try {
            IOSDriver iosDriver = (IOSDriver)this.driver;

            String clipboarddata = iosDriver.getClipboardText();
            logger.info("clipboardData: " + clipboarddata);

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(clipboarddata);
            runTimeData.setKey(testData1.getValue().toString());
            setSuccessMessage("Successfully stored "+clipboarddata+" into ::"+testData1.getValue().toString());
        } catch (Exception e) {
            logger.warn("Exception while executing action" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to copy data from clipboard.Error: " + ExceptionUtils.getMessage(e));
            result = com.testsigma.sdk.Result.FAILED;
        }

	    return result;
  }
}