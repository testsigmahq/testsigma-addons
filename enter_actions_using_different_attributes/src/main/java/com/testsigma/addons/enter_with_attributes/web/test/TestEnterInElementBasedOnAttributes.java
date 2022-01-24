package com.testsigma.addons.enter_with_attributes.web.test;

import com.testsigma.addons.enter_with_attributes.web.EnterInElementBasedOnAttributes;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestEnterInElementBasedOnAttributes {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/input/email/_sample_.Allowing_multiple_e-mail_addresses.html");
    }

    @Test
    public void validateContains() throws Exception {
        EnterInElementBasedOnAttributes nlp = new EnterInElementBasedOnAttributes();
        nlp.setAttribute("id");
        TestData value = new TestData("emailAddre");
        nlp.setValue(value);
        TestData testData = new TestData("abc@mail.com");
        nlp.setTestData(testData);
        nlp.setOperator("contains");
        nlp.setIsLabelText(false);
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        EnterInElementBasedOnAttributes nlp = new EnterInElementBasedOnAttributes();
        nlp.setAttribute("id");
        TestData value = new TestData("emailAddress");
        nlp.setValue(value);
        TestData testData = new TestData("abc@mail.com");
        nlp.setTestData(testData);
        nlp.setOperator("equals");
        nlp.setIsLabelText(false);
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
