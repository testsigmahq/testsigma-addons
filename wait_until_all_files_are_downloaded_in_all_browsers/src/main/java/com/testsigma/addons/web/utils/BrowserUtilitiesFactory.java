package com.testsigma.addons.web.utils;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BrowserUtilitiesFactory {

    public static BrowserUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();

        if (browserName.contains("chrome")) {
            return new ChromeUtilities(driver, logger);
        } else if (browserName.contains("edge")) {
            return new EdgeUtilities(driver, logger);
        } else if (browserName.contains("firefox")) {
            return new FirefoxUtilities(driver, logger);
        } else {
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}