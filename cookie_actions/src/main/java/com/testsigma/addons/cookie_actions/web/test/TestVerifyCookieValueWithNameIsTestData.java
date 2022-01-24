package com.testsigma.addons.cookie_actions.web.test;

import com.testsigma.addons.cookie_actions.web.VerifyCookieValueWithNameIsTestData;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestVerifyCookieValueWithNameIsTestData {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://examples.testsigma.com/");
        Cookie cookie = new Cookie("_acceptCookie", "true");
        driver.manage().addCookie(cookie);
    }

    @Test
    public void validate() throws Exception {
        VerifyCookieValueWithNameIsTestData nlp = new VerifyCookieValueWithNameIsTestData();
        nlp.setCookieName(new TestData("_acceptCookie"));
        nlp.setCookieValue(new TestData("true"));
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
