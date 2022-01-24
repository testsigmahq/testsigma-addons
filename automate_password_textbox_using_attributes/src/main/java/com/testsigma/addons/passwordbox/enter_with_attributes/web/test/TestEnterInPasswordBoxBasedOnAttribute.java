package com.testsigma.addons.passwordbox.enter_with_attributes.web.test;

import com.testsigma.addons.passwordbox.enter_with_attributes.web.EnterInPasswordBoxBasedOnAttribute;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestEnterInPasswordBoxBasedOnAttribute {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/input/password/_sample_.A_simple_password_input.html");
    }

    @Test
    public void validateContains() throws Exception {
        EnterInPasswordBoxBasedOnAttribute nlp = new EnterInPasswordBoxBasedOnAttribute();
        TestData attribute = new TestData("id");
        nlp.setAttribute("id");
        TestData value = new TestData("userPass");
        nlp.setValue(value);
        TestData testData = new TestData("password");
        nlp.setTestData(testData);
        nlp.setOperator("contains");
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        EnterInPasswordBoxBasedOnAttribute nlp = new EnterInPasswordBoxBasedOnAttribute();
        nlp.setAttribute("id");
        TestData value = new TestData("userPassword");
        nlp.setValue(value);
        TestData testData = new TestData("password");
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
