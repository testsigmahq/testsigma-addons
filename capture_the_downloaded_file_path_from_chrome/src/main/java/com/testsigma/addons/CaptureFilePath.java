package com.testsigma.addons;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.testsigma.sdk.Logger;

public class CaptureFilePath {
	WebDriver driver;
    Logger logger;
    public CaptureFilePath(WebDriver driver, Logger logger){
        this.driver = driver;
        this.logger = logger;
    }
	public String copyFileFromDownloads() throws Exception{
	    String currentWindowHandle = driver.getWindowHandle();

	    ((JavascriptExecutor) driver).executeScript("window.open()");
	    Set<String> allWindows = driver.getWindowHandles();
	    ArrayList<String> tabs = new ArrayList<>(allWindows);
	    driver.switchTo().window(tabs.get(tabs.size() - 1));

	    driver.navigate().to("chrome://downloads/");
	    WebDriverWait ww = new WebDriverWait(driver, Duration.ofMillis(60000));
	    ww.until(new ExpectedCondition<Boolean>() {
	        @Override
	        public Boolean apply(WebDriver driver) {
	            return isFileDownloaded();
	        }
	    });
	    String remoteFilePath = null;
	    
	    remoteFilePath = getDownloadedFileLocalPath();
	    
	    logger.info("Downloaded file path="+remoteFilePath);
	    //switch to parent window tab
	    driver.switchTo().window(currentWindowHandle);
	    
		return remoteFilePath;
	}
	    
		private String getDownloadedFileLocalPath() {


	    JavascriptExecutor js = (JavascriptExecutor) driver;
	    Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items[0].filePath; ");
	    if(obj == null){
	        throw new RuntimeException("No files in the downloads");
	    }
	    return obj.toString();
	}
		
		private boolean isFileDownloaded() {

	        if (!driver.getCurrentUrl().startsWith("chrome://downloads")) {
	            driver.get("chrome://downloads/");
	        }
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList')" +
	                ".items.filter(e => e.state === 'IN_PROGRESS').map(e => e.filePath || e.file_path || e.fileUrl || e.file_url); ");
	        if (obj != null && obj instanceof List && !((List) obj).isEmpty()) {
	            return false;
	        }
	        return true;
	    }
}
