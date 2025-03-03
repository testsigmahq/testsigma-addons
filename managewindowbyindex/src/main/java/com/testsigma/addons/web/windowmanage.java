package com.testsigma.addons.web;

import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
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

			int windowIndexToSwitch = Integer.parseInt(testData1.getValue().toString());
			
			driver.switchTo().window(windowHandles.get(windowIndexToSwitch));
			
			Thread.sleep(2000);

			switch (testData2.getValue().toString()) {
			    case "maximize":
			        driver.manage().window().maximize();
			        break;
			    case "minimize":
			    	driver.manage().window().minimize();
			        break;
			}
		} catch(Exception e) { 
            setErrorMessage("inside addon - " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }
        return result;
    }
}