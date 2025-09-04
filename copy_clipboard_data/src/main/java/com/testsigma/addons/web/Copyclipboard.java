package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

@Data
@Action(actionText = "Copy data from the clipboard and store into a runtime variable testdata",
        description = "Copying the data from the clipboard and store into a runtime variable",
        applicationType = ApplicationType.WEB)
public class Copyclipboard extends WebAction {

	@TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testdata;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;
 
  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
	  
	    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
	    logger.info("Initiating execution");

      try {
          String clipboardData = getClipboardData();
          logger.info("clipboardData: " + clipboardData);

          runTimeData = new com.testsigma.sdk.RunTimeData();
          runTimeData.setValue(clipboardData);
          runTimeData.setKey(testdata.getValue().toString());
          setSuccessMessage("Successfully stored "+clipboardData+" into ::"+testdata.getValue().toString());
      } catch (Exception e) {
          logger.warn("Exception while executing action" + ExceptionUtils.getStackTrace(e));
          setErrorMessage("Unable to copy data from clipboard.Error: " + ExceptionUtils.getMessage(e));
          result = com.testsigma.sdk.Result.FAILED;
      }

      return result;
  }
  private static String getClipboardData() {
      try {
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable contents = clipboard.getContents(null);
          if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
              return (String) contents.getTransferData(DataFlavor.stringFlavor);
          }
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      return null;
}
}



