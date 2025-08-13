package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;


@Data
@Action(actionText = "Enter data test-data using keyboard",
        description = "This action allows you to enter data into a field using the keyboard.",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "EnterData"
)
public class EnterDataUsingKeyboard extends WindowsAdvancedAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public com.testsigma.sdk.Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Instantiate the Robot Class
            Robot robot = new Robot();
            StringSelection stringSelection = new StringSelection(testData.getValue().toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable data = clipboard.getContents(null);
            clipboard.setContents(stringSelection, null);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            Thread.sleep(500);
            clipboard.setContents(data, null);
            setSuccessMessage("Given data entered successfully on the given image");
        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("An error occurred while initializing the Robot class: " + e.getMessage());
            logger.debug("Error initializing Robot class: " + ExceptionUtils.getStackTrace(e));
            return result;
        }
        return result;
    }
}
