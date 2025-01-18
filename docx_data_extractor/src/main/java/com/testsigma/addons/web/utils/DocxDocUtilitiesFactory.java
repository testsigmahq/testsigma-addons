package com.testsigma.addons.web.utils;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DocxDocUtilitiesFactory {

    public static DocxDocUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();

        if (browserName.contains("chrome")) {
            return new ChromeDocxUtilities(driver, logger);
        } else if (browserName.contains("edge")) {
            return new EdgeDocxUtilities(driver, logger);
        } else {
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}