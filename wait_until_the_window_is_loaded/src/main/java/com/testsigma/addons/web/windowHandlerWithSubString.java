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
@Action(actionText = "Wait until the window with a title containing testData is present",
        description = "window with the title is present",
        applicationType = ApplicationType.WEB)

public class windowHandlerWithSubString extends WebAction{
	
	
	 @TestData(reference = "testData")
	  private com.testsigma.sdk.TestData testData;

	 public static void waitForWindowWithTitleContaining(WebDriver driver, final String substring, int timeoutInSeconds) {
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
	        wait.until(new ExpectedCondition<Boolean>() {
	            @Override
	            public Boolean apply(WebDriver driver) {
	                for (String windowHandle : driver.getWindowHandles()) {
	                    driver.switchTo().window(windowHandle);
	                    if (driver.getTitle().contains(substring)) {
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
			String substring = testData.getValue().toString();
         
			waitForWindowWithTitleContaining(driver, substring, 60);
                
             } catch (Exception ex) {
                setErrorMessage("Failed get the title of the window" + ex.getMessage());
                return Result.FAILED;
             }
            
             
            return result;
        }
	
	
	
}