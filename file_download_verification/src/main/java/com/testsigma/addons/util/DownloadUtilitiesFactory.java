package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DownloadUtilitiesFactory {

    public static DownloadUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();

        if (browserName.contains("chrome")) {
            logger.info("Chrome browser has been found");
            return new ChromeUtilities(driver, logger);
        } else if (browserName.contains("edge")) {
            logger.info("Edge browser has been found");
            return new EdgeUtilities(driver, logger);
        } else {
            logger.info("Unknown browser has been found");
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}