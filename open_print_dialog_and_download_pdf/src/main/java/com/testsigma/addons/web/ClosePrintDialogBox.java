package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.TracedCommandExecutor;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

@Data
@Action(actionText = "Close print dialog box",
        description = "Closes print dialog box",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ClosePrintDialogBox extends WebAction {

    @Override
    public Result execute() throws NoSuchElementException {
        // Your Awesome code starts here
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        try {
            if (isCloudExecution()) {
                JavascriptExecutor js = (JavascriptExecutor) this.driver;
                logger.info("Pressing tab....");
                js.executeScript("lambda-perform-keyboard-events:{TAB}");
                Thread.sleep(1000);

                logger.info("Pressing enter....");
                js.executeScript("lambda-perform-keyboard-events:{ENTER}");
                Thread.sleep(1000);

                logger.info("Closed the print dialog box successfully");
            } else {
                Robot robot = new Robot();

                // Press TAB
                logger.info("Pressing tab....");
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);
                Thread.sleep(500);

                // Press ENTER
                logger.info("Pressing enter....");
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                Thread.sleep(500);

                logger.info("Closed the print dialog box successfully");
            }

            setSuccessMessage("Closed the print dialog box successfully");

        } catch (RuntimeException e) {
            result = Result.FAILED;
            logger.info("Runtime exception: " + ExceptionUtils.getMessage(e));
        } catch (Exception e) {
            result = Result.FAILED;
            logger.info("Exception occurred " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to perform the operation");
        }
        return result;
    }

    private boolean isCloudExecution() {
        RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
        String remoteAddress;
        try {
            // Get the command executor and check its type
            HttpCommandExecutor httpExecutor;
            if (remoteDriver.getCommandExecutor() instanceof TracedCommandExecutor) {
                // If it's a TracedCommandExecutor, get the delegate field
                TracedCommandExecutor tracedExecutor = (TracedCommandExecutor) remoteDriver.getCommandExecutor();
                Field delegateField = TracedCommandExecutor.class.getDeclaredField("delegate");
                delegateField.setAccessible(true); // Allow access to the private field
                httpExecutor = (HttpCommandExecutor) delegateField.get(tracedExecutor);
            } else if (remoteDriver.getCommandExecutor() instanceof HttpCommandExecutor) {
                // If it's directly an HttpCommandExecutor
                httpExecutor = (HttpCommandExecutor) remoteDriver.getCommandExecutor();
            } else {
                logger.info("Unsupported CommandExecutor type");
                setErrorMessage("Unable to initiate the operation");
                throw new RuntimeException("Unsupported CommandExecutor type");
            }

            // Retrieve the remote address from the executor
            remoteAddress = httpExecutor.getAddressOfRemoteServer().toString();
            if (remoteAddress.toLowerCase().contains("lambdatest")) {
                return true;
            } else {
                logger.info("Unsupported remote address type");
                setErrorMessage("Unable to initiate the operation");
                throw new RuntimeException("Unsupported remote address type");
            }
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
            logger.info("Unable to retrieve remote address");
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to initiate the operation");
            throw new RuntimeException("Couldn't retrieve remote address");
        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
}
