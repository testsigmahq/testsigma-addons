package com.testsigma.addons.click_until.web.test;

import com.testsigma.sdk.Element;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import com.testsigma.addons.click_until.web.ClickUntilTextIsVisible;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestClickUntil {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://www.w3schools.com/html/html_headings.asp");
    }

    @Test
    public void validateNext() throws Exception {
        ClickUntilTextIsVisible nlp = new ClickUntilTextIsVisible();
        nlp.setTestData(new TestData("CSS stands for Cascading Style Sheets."));
        Element element = new Element("", By.xpath("//a[text()='Next ❯']"));
        nlp.setElement(element);
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
