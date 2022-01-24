package com.testsigma.addons.chrome_extension_actions.web.test;

import com.testsigma.addons.chrome_extension_actions.web.EnableInIncognito;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestEnableInIncognito {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialize NLP runner
        driver.get("https://chrome.google.com/webstore/detail/testsigma/epmomlhdjfgdobefcpocockpjihaabdp");
        driver.findElements(By.xpath("//*[@aria-label=\"Add to Chrome\"]")).get(0).click();
    }

    @Test
    public void validateEnable() throws Exception {
        EnableInIncognito nlp = new EnableInIncognito();
        nlp.setName(new TestData("Testsigma"));
        nlp.setEnable(new TestData(true));
        //place a debugger here, pause and manually click add extension button
        runner.run(nlp);
    }

    @Test
    public void validateDisable() throws Exception {
        EnableInIncognito nlp = new EnableInIncognito();
        nlp.setName(new TestData("Testsigma"));
        nlp.setEnable(new TestData(false));
        //place a debugger here, pause and manually click add extension button
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
