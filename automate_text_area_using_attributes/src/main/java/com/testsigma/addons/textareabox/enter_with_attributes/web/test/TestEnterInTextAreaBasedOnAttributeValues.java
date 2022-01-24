package com.testsigma.addons.textareabox.enter_with_attributes.web.test;

import com.testsigma.addons.textareabox.enter_with_attributes.web.EnterInTextAreaBasedOnAttributeValues;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestEnterInTextAreaBasedOnAttributeValues {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/API/HTMLTextAreaElement/_sample_.Autogrowing_textarea_example.html");
    }

    @Test
    public void validateContains() throws Exception {
        EnterInTextAreaBasedOnAttributeValues nlp = new EnterInTextAreaBasedOnAttributeValues();
        nlp.setAttribute("class");
        TestData value = new TestData("noscroll");
        nlp.setValue(value);
        TestData testData = new TestData("comments");
        nlp.setTestData(testData);
        TestData operator = new TestData("contains");
        nlp.setTestData(operator);
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        EnterInTextAreaBasedOnAttributeValues nlp = new EnterInTextAreaBasedOnAttributeValues();
        nlp.setAttribute("class");
        TestData value = new TestData("noscroll");
        nlp.setValue(value);
        TestData testData = new TestData("comments");
        nlp.setTestData(testData);
        TestData operator = new TestData("equals");
        nlp.setTestData(operator);
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
