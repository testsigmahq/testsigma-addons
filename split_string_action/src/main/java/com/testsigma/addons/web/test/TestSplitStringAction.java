package com.testsigma.addons.web.test;

import com.testsigma.addons.web.SplitStringAction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestSplitStringAction {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "/Users/vikramramalingam/Documents/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver);
        driver.get("https://www.google.com");
    }

    @Test
    public void validateSplitString() throws Exception {
        SplitStringAction action = new SplitStringAction();
        action.setInputValue(new TestData("https://www.google.com/"));
        action.setRegex(new TestData(":"));
        action.setPosition(new TestData("1")); // You can specify position from 1.
        action.setExtractedString(new TestData("output"));
        runner.run(action);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
