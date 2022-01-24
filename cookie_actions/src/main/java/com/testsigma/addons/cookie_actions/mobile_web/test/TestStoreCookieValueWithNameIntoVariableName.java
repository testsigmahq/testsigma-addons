package com.testsigma.addons.cookie_actions.mobile_web.test;

import com.testsigma.addons.cookie_actions.mobile_web.StoreCookieValueWithNameIntoVariableName;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestStoreCookieValueWithNameIntoVariableName {
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
    }

    @Test
    public void validate() throws Exception {
        StoreCookieValueWithNameIntoVariableName storeNlp = new StoreCookieValueWithNameIntoVariableName();
        storeNlp.setCookieName(new TestData("_acceptCookie"));
        storeNlp.setCookieValue(new TestData("true"));
        runner.run(storeNlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
