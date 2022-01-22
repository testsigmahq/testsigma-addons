package com.testsigma.addons.textbox.enter_with_attributes.web.test;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import com.testsigma.addons.textbox.enter_with_attributes.web.EnterInTextBoxBasedOnAttributes;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestEnterInTextBoxBasedOnAttributes {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/input/text/_sample_.Basic_example.html");
    }

    @Test
    public void validateContains() throws Exception {
        EnterInTextBoxBasedOnAttributes nlp = new EnterInTextBoxBasedOnAttributes();
        nlp.setAttribute("id");
        TestData value = new TestData("unam");
        nlp.setValue(value);
        TestData testData = new TestData("username");
        nlp.setTestData(testData);
        nlp.setOperator("contains");
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        EnterInTextBoxBasedOnAttributes nlp = new EnterInTextBoxBasedOnAttributes();
        nlp.setAttribute("id");
        TestData value = new TestData("uname");
        nlp.setValue(value);
        TestData testData = new TestData("username");
        nlp.setTestData(testData);
        nlp.setOperator("equals");
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
