package com.testsigma.addons.local_storage.mobile_web.test;

import com.testsigma.addons.local_storage.mobile_web.ClearLocalStorage;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.html5.WebStorage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TestClearLocalStorage {
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
        ClearLocalStorage nlp = new ClearLocalStorage();
        runner.run(nlp);
        Set<String> keys = ((WebStorage) driver).getLocalStorage().keySet();
        for (String key : keys) System.out.println(((WebStorage) driver).getLocalStorage().getItem(key));
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
