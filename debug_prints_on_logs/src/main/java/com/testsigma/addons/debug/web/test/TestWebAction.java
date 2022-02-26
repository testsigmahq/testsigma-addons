package com.testsigma.addons.debug.web.test;

import com.testsigma.addons.debug.web.DebugCookieValue;
import com.testsigma.sdk.runners.ActionRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestWebAction {
  private ActionRunner runner;
  private ChromeDriver driver;

  @BeforeClass
  public void setup() throws Exception {
    System.setProperty("webdriver.chrome.driver", "C:\\Users\\Lenovo\\Downloads\\cdriver\\chromedriver.exe");
    driver = new ChromeDriver();
    driver.manage().deleteAllCookies();
    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    runner = new ActionRunner(driver); //Initialie Action runner
    driver.get("https://opensource-demo.orangehrmlive.com/index.php/auth/login");
    driver.findElement(By.xpath("//input[@id='txtUsername']")).sendKeys("Admin");
    driver.findElement(By.xpath("//input[@id='txtPassword']")).sendKeys("admin123");
    driver.findElement(By.xpath("//input[@id='btnLogin']")).click();
    driver.findElement(By.xpath("//b[normalize-space()='Admin']")).click();
  }

  @Test
  public void validateCountriesCount() throws Exception {
    //PrintOptions action=new PrintOptions();
    //OptionsfromList action=new OptionsfromList();
    //PrintSelectedoption action=new PrintSelectedoption();
    //PrintCountofwindow action=new PrintCountofwindow();
    //PrintCookies action=new PrintCookies();
    DebugCookieValue action = new DebugCookieValue();


    //Element element = new Element("",By.xpath("//select[@id='searchSystemUser_userType']"));
    //action.setElement(element);
    runner.run(action);
  }

  @AfterClass
  public void teardown() {
    if (runner != null) {
      runner.quit();
    }
  }
}
