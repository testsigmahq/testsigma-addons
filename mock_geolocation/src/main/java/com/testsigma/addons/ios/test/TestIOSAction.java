package com.testsigma.addons.ios.test;


import com.testsigma.addons.ios.SetGeoLocation;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
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
        runner = new ActionRunner(driver);

    }

    @Test
    public void executetestios() throws Exception {
        SetGeoLocation mock1 = new SetGeoLocation();
        mock1.setTestData1(new TestData("76"));
        mock1.setTestData2(new TestData("116"));
        mock1.setTestData3(new TestData("0"));
        runner.run(mock1);

    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
