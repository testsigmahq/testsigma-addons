package com.testsigma.addons.emailbox.enter_with_attributes.web.test;

import com.testsigma.addons.emailbox.enter_with_attributes.web.*;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestEnterInEmailBoxBasedOnAttributeValues {
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
    public void validateEnterInEmailBoxWithFollowingText() throws Exception {
        driver.get("https://www.instagram.com/?hl=en");
        EnterInEmailBoxWithFollowingText nlp = new EnterInEmailBoxWithFollowingText();
        nlp.setTestData(new TestData("xrttfyu"));
        nlp.setValue(new TestData("Password"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithPrecedingText() throws Exception {
        driver.get("https://www.instagram.com/?hl=en");
        EnterInEmailBoxWithPrecedingText nlp = new EnterInEmailBoxWithPrecedingText();
        nlp.setTestData(new TestData("xrttfyu"));
        nlp.setValue(new TestData("Password"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithPrecedingTextContaining() throws Exception {
        driver.get("https://www.instagram.com/?hl=en");
        EnterInEmailBoxWithPrecedingTextContaining nlp = new EnterInEmailBoxWithPrecedingTextContaining();
        nlp.setTestData(new TestData("xrttfyu"));
        nlp.setValue(new TestData("Passwo"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithFollowingTextContaining() throws Exception {
        driver.get("https://www.instagram.com/?hl=en");
        EnterInEmailBoxWithFollowingTextContaining nlp = new EnterInEmailBoxWithFollowingTextContaining();
        nlp.setTestData(new TestData("xrttfyu"));
        nlp.setValue(new TestData("Passwo"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithFollowingLabelText() throws Exception {
        driver.get("https://examples.testsigma.com/signup");
        EnterInEmailBoxWithFollowingLabelText nlp = new EnterInEmailBoxWithFollowingLabelText();
        nlp.setTestData(new TestData("xrttfyu"));
        nlp.setValue(new TestData("Full name"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithFollowingLabelTextContaining() throws Exception {
        driver.get("https://examples.testsigma.com/signup");
        EnterInEmailBoxWithFollowingLabelTextContaining nlp = new EnterInEmailBoxWithFollowingLabelTextContaining();
        nlp.setTestData(new TestData("xrttfyu"));
        nlp.setValue(new TestData("Full nam"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithPrecedingLabelText() throws Exception {
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/label/_sample_.Using_the_for_attribute.html");
        EnterInEmailBoxWithPrecedingText nlp = new EnterInEmailBoxWithPrecedingText();
        nlp.setTestData(new TestData("input"));
        nlp.setValue(new TestData("Click me"));
        runner.run(nlp);
    }

    @Test
    public void validateEnterInEmailBoxWithPrecedingLabelTextContaining() throws Exception {
        driver.get("https://yari-demos.prod.mdn.mozit.cloud/en-US/docs/Web/HTML/Element/label/_sample_.Using_the_for_attribute.html");
        EnterInEmailBoxWithPrecedingTextContaining nlp = new EnterInEmailBoxWithPrecedingTextContaining();
        nlp.setTestData(new TestData("input"));
        nlp.setValue(new TestData("Click m"));
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
