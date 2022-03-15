package com.testsigma.addons.web.test;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.Element;
import com.testsigma.sdk.runners.ActionRunner;
import com.testsigma.addons.web.ConditionalStringCompareAndroid;
import com.testsigma.addons.web.CondtionalStringCompareWeb;
import com.testsigma.addons.web.StringCompareWeb;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestWebAction {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Amit\\Downloads\\chromedriver_win32 (1)\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialie Action runner
        driver.get("https://www.orangehrm.com/orangehrm-30-day-trial/");
    }
    @Test
    public void validateCountriesCount() throws Exception {
    	CondtionalStringCompareWeb action = new CondtionalStringCompareWeb();
    	
      //action.setTestData1(new TestData("test123"));
     // action.setTestData2(new TestData("test123"));
       
       
        runner.run(action);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
