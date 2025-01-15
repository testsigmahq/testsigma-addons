package com.testsigma.addons.android;

import com.testsigma.addons.android.util.KeyUtil;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;

@Data
@Action(actionText = "Press Control key with other control key combination, Control key 1: Control-Key , Combination key: Combination-key",
        description = "Allows to press control key and a combination key in a single action. For example, to press Control + A, Control key should be 'Control' and Combination key should be 'a'",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class PressControlOrFunctionKeyWithOtherControlKey extends AndroidAction {

  @TestData(reference = "Control-Key",allowedValues =
          {
                  "Tab","Enter","Caps-Lock","Shift","Control","AltKey","Backspace","Delete","Escape","Insert","Home","End","Page-Up","Page-Down","Space","Comma","Period","Minus","Equals","Colon","Semi-Colon","Slash","Back-Slash","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","Print-Screen","Scroll-Lock","Pause-Break"
          })
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "Combination-key",allowedValues =
          {
                  "Tab","Enter","Caps-Lock","Shift","Control","AltKey","Backspace","Delete","Escape","Insert","Home","End","Page-Up","Page-Down","Space","Comma","Period","Minus","Equals","Colon","Semi-Colon","Slash","Back-Slash","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","Print-Screen","Scroll-Lock","Pause-Break"
          })

  private com.testsigma.sdk.TestData combinationKey;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
      String controlKey = testData.getValue().toString();
      String combinationKey = this.combinationKey.getValue().toString();
      String[] combinationKeys = {controlKey,combinationKey};
      Robot robot = new Robot();
      robot.delay(100); // Delay to switch to the application where you want to paste the text
      KeyUtil keyUtil = new KeyUtil(logger);
      keyUtil.pressAndReleaseInCombination(robot, combinationKeys);

      setSuccessMessage("Successfully performed key press");

    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info("Unable to perform key press:"+ExceptionUtils.getStackTrace(error));
      setErrorMessage("Unable to perform key press:"+error.getMessage());
    }
    return result;
  }
}