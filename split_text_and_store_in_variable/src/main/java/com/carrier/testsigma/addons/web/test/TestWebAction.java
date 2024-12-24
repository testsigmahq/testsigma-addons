package com.carrier.testsigma.addons.web.test;

import com.carrier.testsigma.addons.web.MyFirstWebAction;

import com.testsigma.sdk.Element;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class TestWebAction {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "<Chrome Driver Path>");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        runner = new ActionRunner(driver); //Initialie Action runner
        driver.get("https://www.orangehrm.com/orangehrm-30-day-trial/");
    }
    @Test
    public void validateCountriesCount() throws Exception {
        MyFirstWebAction action = new MyFirstWebAction();
        action.setTestData(new TestData("232"));
        Element element = new Element("",By.name("Country"));
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
