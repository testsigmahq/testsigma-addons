package com.carrier.testsigma.addons.ios.test;


import com.google.common.collect.ImmutableMap;
import com.carrier.testsigma.addons.ios.EnterDataIfVisible;

import com.testsigma.sdk.Element;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


public class TestIOSAction {
    private ActionRunner runner;
    private IOSDriver driver;

    @BeforeClass
    public void setup() throws Exception {
       // Make sure to start Appium server
       DesiredCapabilities capabilities = new DesiredCapabilities();

       capabilities.setCapability("platformName", "iOS");

       Map<String, Object> appiumOptions = new HashMap<>();
       appiumOptions.put("platformVersion", "14");
       appiumOptions.put("automationName", "XCUITest");
       appiumOptions.put("deviceName", "iPhone 11 Max");
       appiumOptions.put("app", "<IPA FILE PATH>");
       capabilities.setCapability("appium:options", appiumOptions);

       IOSDriver driver = new IOSDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
       driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
       driver.executeScript("mobile: activateApp", ImmutableMap.of("bundleId",
               driver.getCapabilities().getCapability("appium:bundleId")));

       runner = new ActionRunner(driver); //Initialie Action runner
    }

    @Test
    public void enterUserName() throws Exception {
        EnterDataIfVisible action = new EnterDataIfVisible();
        action.setTestData(new TestData("testUser"));
        Element element = new Element("", By.xpath("//XCUIElementTypeTextField[@id='userName']"));
        action.setElement(element);
        runner.run(action);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
