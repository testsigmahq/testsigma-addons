package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DownloadUtilitiesFactory {

    public static DownloadUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();

        if (browserName.contains("chrome")) {
            return new ChromeDownloadUtilities(driver, logger);
        } else if (browserName.contains("edge")) {
            return new EdgeDownloadUtilities(driver, logger);
        } else {
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}


