package com.testsigma.addons.windowsLite;

import com.testsigma.addons.util.KeyboardUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.event.KeyEvent;


@Data
@Action(actionText = "Enter data test-data using keyboard",
        description = "This action allows you to enter data into a field using the keyboard. " +
                "This works only for local executions",
        applicationType = ApplicationType.WINDOWS,
        displayName = "Enter Data on screen and press Enter",
        useCustomScreenshot = true
)
public class EnterDataUsingKeyboardAndPressEnter extends WindowsAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            String text = testData.getValue().toString();

            Thread.sleep(2000); // Wait 2 seconds to focus the target window

            for (char c : text.toCharArray()) {
                KeyboardUtils.typeCharacter(robot, c);
                Thread.sleep(50); // Delay between keystrokes (optional)
            }
            logger.info("Pressing Enter key");
            robot.keyPress(KeyEvent.VK_ENTER);
            KeyboardUtils.sleep(30);
            robot.keyRelease(KeyEvent.VK_ENTER);

            setSuccessMessage("Given data entered successfully on the given image and Enter key pressed");

            // Capture and upload screenshot
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult, "enter_data_screenshot", logger);

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("An error occurred while initializing the Robot class: " + e.getMessage());
            logger.debug("Error initializing Robot class: " + ExceptionUtils.getStackTrace(e));
            // Capture and upload screenshot even on failure
            ScreenshotUtils.captureAndUploadScreenshot(testStepResult,
                    "enter_data_failure_screenshot", logger);
            return result;
        }
        return result;
    }

}
