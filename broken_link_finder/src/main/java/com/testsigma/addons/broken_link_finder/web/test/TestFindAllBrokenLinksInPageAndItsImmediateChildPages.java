package com.testsigma.addons.broken_link_finder.web.test;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import com.testsigma.addons.broken_link_finder.web.FindAllBrokenLinksInPageAndItsImmediateChildPages;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestFindAllBrokenLinksInPageAndItsImmediateChildPages {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialie NLP runner
        driver.get("https://www.orangehrm.com");
    }

    @Test
    public void validate() throws Exception {
        FindAllBrokenLinksInPageAndItsImmediateChildPages nlp = new FindAllBrokenLinksInPageAndItsImmediateChildPages();
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
