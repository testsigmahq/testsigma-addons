package com.testsigma.addons.broken_link_finder.web.test;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import com.testsigma.addons.broken_link_finder.web.ReportErrorsInPage;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestReportErrorsInPage {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
    }

    @Test
    public void validate() throws Exception {
        ReportErrorsInPage nlp = new ReportErrorsInPage();
        nlp.setURL(new TestData("https://www.orangehrm.com"));
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
