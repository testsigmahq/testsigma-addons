package com.testsigma.addons.web.test;

import com.testsigma.addons.web.PasteCopiedValueAction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.Element;
import com.testsigma.sdk.runners.ActionRunner;
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
        System.setProperty("webdriver.chrome.driver", "/Users/vikramramalingam/Documents/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialie Action runner
        driver.get("https://www.google.com");
    }
    @Test
    public void validateCountriesCount() throws Exception {
        PasteCopiedValueAction action = new PasteCopiedValueAction();
        Element element = new Element("",By.xpath("//input[@class=\"gLFyf gsfi\"]"));
        action.setElement(element);
        runner.run(action);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
