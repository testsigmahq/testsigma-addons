package com.testsigma.addons.web;

import java.time.Duration;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;

import lombok.Data;


@Data
@Action(actionText = "Wait until the window with the title testData is present",
        description = "window with the title is present",
        applicationType = ApplicationType.WEB)

public class WindowHandler extends WebAction{
	
	
	 @TestData(reference = "testData")
	  private com.testsigma.sdk.TestData testData;

    public void waitForWindowWithTitle(WebDriver driver,int timeoutInSeconds,final String title) {
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(timeoutInSeconds));
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                for (String windowHandle : driver.getWindowHandles()) {
                    driver.switchTo().window(windowHandle);
                    String driverTitle = driver.getTitle();
                    logger.info(String.format("Checking driver title %s has text %s", driverTitle, title));
                    if (driverTitle.contains(title)) {
                    	logger.info(String.format("Found title %s, contains text %s", driverTitle, title));
                        return true;
                    	
                    }
                }
                return false;
            }
        });
    }
    
	@Override
	protected Result execute() throws NoSuchElementException {
		    
		Result result = Result.SUCCESS;
		try {
			String title = testData.getValue().toString();
         
			 waitForWindowWithTitle(driver, 60, title);
                
             } catch (Exception ex) {
                setErrorMessage("Failed get the title of the window" + ex.getMessage());
                return Result.FAILED;
             }
            
             
            return result;
        }
	
	
	
}