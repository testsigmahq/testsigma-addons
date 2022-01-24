package com.testsigma.addons.radio.with_attributes.web.test;

import com.testsigma.addons.radio.with_attributes.web.ClickOnRadioButtonBasedOnAttribute;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestClickOnRadioButtonBasedOnAttribute {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/input/radio/_sample_.Data_representation_of_a_radio_group.html");
    }

    @Test
    public void validateContains() throws Exception {
        ClickOnRadioButtonBasedOnAttribute nlp = new ClickOnRadioButtonBasedOnAttribute();
        nlp.setAttribute("value");
        TestData value = new TestData("phon");
        nlp.setTestData(value);
        nlp.setOperator("contains");
        runner.run(nlp);
    }

    @Test
    public void validateEquals() throws Exception {
        ClickOnRadioButtonBasedOnAttribute nlp = new ClickOnRadioButtonBasedOnAttribute();
        nlp.setAttribute("value");
        TestData value = new TestData("phone");
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
