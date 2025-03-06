package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;


@Data
@Action(actionText = "Switch to window by index testdata and selectable-list the window",
        description = "Switch to window by index and manage the window maximize/minimize",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false )
public class windowmanage extends WebAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "selectable-list", allowedValues = {"maximize", "minimize"})
    private com.testsigma.sdk.TestData testData2;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");

        try {
            List<String> windowHandles = new java.util.ArrayList<>(driver.getWindowHandles());
            logger.info("Available window handles: " + windowHandles);

            int windowIndexToSwitch = Integer.parseInt(testData1.getValue().toString());
            logger.info("Attempting to switch to window index: " + windowIndexToSwitch);

            if (windowIndexToSwitch < 0 || windowIndexToSwitch >= windowHandles.size()) {
                String errorMessage = String.format("Invalid window index: %d. Window index should be between 0 and %d (inclusive).", windowIndexToSwitch, windowHandles.size() - 1);
                setErrorMessage(errorMessage);
                result = Result.FAILED;
                logger.warn(errorMessage);  // Use logger.warn for invalid input
                return result;
            }

            driver.switchTo().window(windowHandles.get(windowIndexToSwitch));
            logger.info("Successfully switched to window index: " + windowIndexToSwitch);

            Thread.sleep(2000);


            switch (testData2.getValue().toString()) {
                case "maximize":
                    driver.manage().window().maximize();
                    setSuccessMessage(String.format("Successfully maximized window with index %d.", windowIndexToSwitch));
                    logger.info("Successfully maximized window with index: " + windowIndexToSwitch);
                    break;
                case "minimize":
                    driver.manage().window().minimize();
                    setSuccessMessage(String.format("Successfully minimized window with index %d.", windowIndexToSwitch));
                    logger.info("Successfully minimized window with index: " + windowIndexToSwitch);
                    break;
            }
        } catch(NumberFormatException e) {
            String errorMessage = "Invalid window index format. Please enter a valid number.";
            setErrorMessage(errorMessage);
            result = Result.FAILED;
            logger.warn(errorMessage + e);
        }
        catch (Exception e) {
            String errorMessage = "An unexpected error occurred: " + e.getMessage();
            setErrorMessage(errorMessage);
            result = Result.FAILED;
            logger.warn(errorMessage + e);
        }
        return result;
    }
}