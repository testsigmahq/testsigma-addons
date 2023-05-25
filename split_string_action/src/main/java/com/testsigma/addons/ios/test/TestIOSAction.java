package com.testsigma.addons.ios.test;


import com.testsigma.sdk.TestData;
import com.testsigma.sdk.Element;
import com.testsigma.sdk.runners.ActionRunner;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import com.testsigma.addons.ios.EnterDataIfVisible;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;


public class TestIOSAction {
    private ActionRunner runner;
    private IOSDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        //Make sure to start Appium server
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "14");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "iPhone 11 Max");

        capabilities.setCapability("app", "<IPA FILE PATH>");

        IOSDriver<WebElement> driver = new IOSDriver<>(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        driver.launchApp();
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
