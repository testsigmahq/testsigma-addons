package com.testsigma.addons.developer_console_actions.mobile_web.test;

import com.testsigma.addons.developer_console_actions.mobile_web.VerifyThereAreNoErrorsOnConsole;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestVerifyThereAreNoErrorsOnConsole {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://travel.testsigma.com/");
    }

    @Test
    public void validate() throws Exception {
        VerifyThereAreNoErrorsOnConsole nlp = new VerifyThereAreNoErrorsOnConsole();
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
