package com.testsigma.addons.local_storage.web.test;

import com.testsigma.addons.local_storage.web.VerifyThatValueOfKeyLocalStorageIsTestData;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.html5.WebStorage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TestVerifyThatValueOfKeyLocalStorageIsTestData {
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
        VerifyThatValueOfKeyLocalStorageIsTestData nlp = new VerifyThatValueOfKeyLocalStorageIsTestData();
        nlp.setKey(new TestData("items"));
        nlp.setExpectedValue(new TestData("{\"dr_city\":\"LA-US\",\"ar_city\":\"LD-UK\",\"dr_date\":\"December 16 2021\",\"tripType\":1,\"guestCounter_c\":0,\"guestCounter_i\":0,\"guestCounter_a\":1,\"adultlist\":1,\"total_guest\":1}"));
        runner.run(nlp);
        Set<String> keys = ((WebStorage) driver).getLocalStorage().keySet();
        for (String key : keys) System.out.println(((WebStorage) driver).getLocalStorage().getItem(key));
        System.out.println(keys);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
