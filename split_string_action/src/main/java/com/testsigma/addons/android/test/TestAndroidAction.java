package com.testsigma.addons.android.test;


import com.testsigma.sdk.TestData;
import com.testsigma.sdk.runners.ActionRunner;
import io.appium.java_client.android.AndroidDriver;
import com.testsigma.addons.android.SwipeOnScreen;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TestAndroidAction {
    private ActionRunner runner;
    private AndroidDriver driver;

    @BeforeClass
    public void setup() throws Exception {
        //Make sure to start Appium server
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("app", "<APP_PATH>");
        caps.setCapability("deviceName", "<DEVICE_NAME>");
        caps.setCapability("udid", "<DEVICE_UDID>");
        caps.setCapability("platformName", "android");
        driver = new AndroidDriver<>(new URL("http://localhost:4723/wd/hub"), caps);
        driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS);
        driver.launchApp();
        runner = new ActionRunner(driver); //Initialie Action runner

    }

    @Test
    public void swipeOnScreen() throws Exception {
        SwipeOnScreen action = new SwipeOnScreen();
        action.setSourceCoordinates(new TestData("102,60"));
        action.setTargetCoordinates(new TestData("102,170"));
        runner.run(action);
    }

    @AfterClass
    public void teardown() {
        if (runner != null) {
            runner.quit();
        }
    }
}
