package com.testsigma.addons.test;

import com.testsigma.addons.ClickOnElementBasedOnAttributeValues;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestClickOnElementBasedOnAttributeValues {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://examples.testsigma.com/login");
    }

    @Test
    public void validateContains() throws Exception {
        ClickOnElementBasedOnAttributeValues nlp = new ClickOnElementBasedOnAttributeValues();
        nlp.setAttribute("text");
        TestData value = new TestData("Logi");
        nlp.setValue(value);
        nlp.setOperator("contains");
        nlp.setElementType("button");
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        ClickOnElementBasedOnAttributeValues nlp = new ClickOnElementBasedOnAttributeValues();
        nlp.setAttribute("text");
        TestData value = new TestData("Login");
        nlp.setValue(value);
        nlp.setOperator("equals");
        nlp.setElementType("button");
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
