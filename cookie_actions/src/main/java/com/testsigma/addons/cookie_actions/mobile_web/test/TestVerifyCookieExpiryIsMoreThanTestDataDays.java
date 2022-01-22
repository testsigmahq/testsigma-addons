package com.testsigma.addons.cookie_actions.mobile_web.test;

import com.testsigma.addons.cookie_actions.mobile_web.VerifyCookieExpiryIsMoreThanTestDataDays;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestVerifyCookieExpiryIsMoreThanTestDataDays {
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
        Date today = new Date();
        today.setDate(today.getDate() + 7);
        Cookie cookie = new Cookie("_acceptCookie", "true", "/", today);
        driver.manage().addCookie(cookie);
    }

    @Test
    public void validate() throws Exception {
        VerifyCookieExpiryIsMoreThanTestDataDays nlp = new VerifyCookieExpiryIsMoreThanTestDataDays();
        nlp.setCookieName(new TestData("_acceptCookie"));
        nlp.setNumberOfDays(new TestData("5"));
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
