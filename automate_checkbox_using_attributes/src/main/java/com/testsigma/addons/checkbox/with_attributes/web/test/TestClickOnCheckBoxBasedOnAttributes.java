package com.testsigma.addons.checkbox.with_attributes.web.test;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import com.testsigma.addons.checkbox.with_attributes.web.ClickOnCheckBoxBasedOnAttributes;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestClickOnCheckBoxBasedOnAttributes {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/input/checkbox/_sample_.Handling_multiple_checkboxes.html");
    }

    @Test
    public void validateContains() throws Exception {
        ClickOnCheckBoxBasedOnAttributes nlp = new ClickOnCheckBoxBasedOnAttributes();
        nlp.setAttribute("id");
        TestData value = new TestData("codi");
        nlp.setTestData(value);
        nlp.setOperator("contains");
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        ClickOnCheckBoxBasedOnAttributes nlp = new ClickOnCheckBoxBasedOnAttributes();
        nlp.setAttribute("id");
        TestData value = new TestData("coding");
        nlp.setTestData(value);
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
