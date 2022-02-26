package com.testsigma.addons.debug.web.test;

import com.testsigma.sdk.runners.ActionRunner;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;

public class TestMobile {
  public static final String URL = "https://krukmangada:846508c1-b42d-4fc7-a1e1-0770df454237@ondemand.us-west-1.saucelabs.com:443/wd/hub";
  private ActionRunner runner;
  private AndroidDriver driver;

  @BeforeClass
  public void setup() throws Exception {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability("platformName", "Android");
    capabilities.setCapability("deviceName", "Google_Pixel_4a_real_us");
    capabilities.setCapability("platformVersion", "11");
    capabilities.setCapability("app", "");
    capabilities.setCapability("browserName", "Chrome");
    capabilities.setCapability("deviceOrientation", "portrait");
    AndroidDriver<WebElement> driver = new AndroidDriver<WebElement>(new URL(URL), capabilities);
    driver.get("https://www.amazon.com");
    //driver.launchApp();

  }

  @Test
  public void validateCountriesCount() throws Exception {
    driver.get("https://www.amazon.com");
    System.out.println("OVER and OUT");
    //PrintOptions action=new PrintOptions();
    //OptionsfromList action=new OptionsfromList();
    //PrintSelectedoption action=new PrintSelectedoption();
    //PrintCountofwindow action=new PrintCountofwindow();
    //PrintCookies action=new PrintCookies();
    //CookieValue action= new CookieValue();


    //Element element = new Element("",By.xpath("//select[@id='searchSystemUser_userType']"));
    //action.setElement(element);
    //runner.run(action);
  }

  @AfterClass
  public void teardown() {

    driver.quit();

  }
}
