package com.testsigma.addons.pdf.web.test;

import com.testsigma.addons.pdf.web.ContainsTextpdf;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestContainsTextpdf {
    private ActionRunner runner;
    private ChromeDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Lenovo\\Documents\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        runner = new ActionRunner(driver); //Initialie NLP runner
        driver.get("https://api.printnode.com/static/test/pdf/multipage.pdf");

    }
    @Test
    public void pdfcontainstext() throws Exception {
        ContainsTextpdf nlp = new ContainsTextpdf();
        nlp.setTestData(new TestData("Test"));
        runner.run(nlp);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
