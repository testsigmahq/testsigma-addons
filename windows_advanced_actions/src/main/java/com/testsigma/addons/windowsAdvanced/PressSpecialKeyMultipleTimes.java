package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.windowsAdvanced.util.KeyUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.awt.*;

@Data
@Action(actionText = "Press special key Special-Key no of times No-Of-Times",
        description = "Allows to press control or function key multiple times. For example, to press Control key 5 times, Control key should be 'Control' and No-Of-Times should be '5'",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Press special key No-Of-Times",
        useCustomScreenshot = false)
public class PressSpecialKeyMultipleTimes extends WindowsAdvancedAction {

    @TestData(reference = "Special-Key", allowedValues =
            {
                    "Arrow-Left", "Arrow-Right", "Arrow-Up", "Arrow-Down"
            })
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "No-Of-Times")
    private com.testsigma.sdk.TestData noOfTimes;


    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            String data = testData.getValue().toString();
            String[] combinationKeys = {data};

            Robot robot = new Robot();
            robot.delay(100); // Delay to switch to the application where you want to paste the text
            int count = Integer.parseInt(this.noOfTimes.getValue().toString());
            KeyUtil keyUtil = new KeyUtil(logger);
            for (int i = 0; i < count; i++) {
                keyUtil.pressAndReleaseInCombination(robot, combinationKeys);
                robot.delay(10);
            }
            setSuccessMessage("Successfully performed key press");

        } catch (Exception error) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Unable to perform key press:" + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Unable to perform key press:" + error.getMessage());
        }
        return result;
    }
}