package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.windowsAdvanced.util.KeyUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

@Data
@Action(actionText = "Press Control key: Control-Key with Combination key: Combination-key and store clipboard text in runtime variable: variable_name",
        description = "Allows to press Ctrl+C (copy) or Ctrl+V (paste) in a single action, then reads the " +
                "system clipboard afterwards and stores its text content into a runtime variable. " +
                "Use Ctrl+C to capture newly copied text into variable_name. Ctrl+V is also supported so you " +
                "can confirm/log what text was just pasted, but note it reads whatever text was already on the " +
                "clipboard before the paste, since pasting does not change clipboard content.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Press Control key with Combination key and store clipboard text in runtime variable",
        useCustomScreenshot = true)
public class PressControlOrFunctionKeyWithCombinationandstore extends WindowsAdvancedAction {

  @TestData(reference = "Control-Key",allowedValues =
          {
                  "Control"
          })
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "Combination-key",allowedValues =
          {
                  "c","v"
          })
  private com.testsigma.sdk.TestData combinationKey;

  // Name of the runtime variable to store the copied clipboard text into.
  @TestData(reference = "variable_name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
      String controlKey = testData.getValue().toString();
      String combinationKey = this.combinationKey.getValue().toString();
      String[] combinationKeys = {controlKey, combinationKey};
      Robot robot = new Robot();
      robot.delay(100); // Delay to switch to the application where you want to paste the text
      KeyUtil keyUtil = new KeyUtil(logger);
      keyUtil.pressAndReleaseInCombination(robot, combinationKeys);

      // Give the OS a brief moment to populate the clipboard after the key combo (e.g. Ctrl+C) fires.
      robot.delay(200);

      String copiedText = readClipboardText();
      logger.info("Text read from clipboard: " + copiedText);

      String varName = this.variableName.getValue().toString();

      if (copiedText == null || copiedText.isEmpty()) {
        setSuccessMessage("Performed Ctrl action but no data is stored as no clipboard data is present");
      } else {
        // Correct RunTimeData API: set the key (runtime variable name) and value separately.
        runTimeData.setKey(varName);
        runTimeData.setValue(copiedText);

        setSuccessMessage("Successfully performed key press and stored clipboard text \"" + copiedText
                + "\" in runtime variable \"" + varName + "\"");
      }

    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info("Unable to perform key press and store clipboard text:" + ExceptionUtils.getStackTrace(error));
      setErrorMessage("Unable to perform key press and store clipboard text:" + error.getMessage());
    }
    return result;
  }

  /**
   * Reads plain text currently on the system clipboard.
   * Returns an empty string if the clipboard has no text content (e.g. nothing was selected before Ctrl+C).
   */
  private String readClipboardText() throws Exception {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
      Object data = clipboard.getData(DataFlavor.stringFlavor);
      return data != null ? data.toString() : "";
    }
    logger.info("Clipboard does not contain text content after key press.");
    return "";
  }
}
