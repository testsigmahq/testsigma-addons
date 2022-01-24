package com.testsigma.addons.pdf.web.test;

import com.testsigma.addons.pdf.web.NoofCharbeforetext;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestNoofcharbeforetext {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Lenovo\\Documents\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialie NLP runner
        driver.get("https://www.clickdimensions.com/links/TestPDFfile.pdf");
    }

    @Test
    public void noofcharbeforetextpdf() throws Exception {
        NoofCharbeforetext nlp = new NoofCharbeforetext();
        nlp.setTestData1(new TestData("4"));
        nlp.setTestData2(new TestData("test"));
        nlp.setTestData3(new TestData("Var1"));
        runner.run(nlp);

    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
