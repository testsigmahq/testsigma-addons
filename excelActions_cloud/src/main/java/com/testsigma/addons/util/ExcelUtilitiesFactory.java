package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ExcelUtilitiesFactory {

    public static ExcelUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();
        logger.info("browserName: " + browserName);

        if (browserName.toLowerCase().contains("chrome")) {
            logger.info("Chrome browser found");
            return new ChromeExcelUtilities(driver, logger);
        } else if (browserName.toLowerCase().contains("edge")) {
            logger.info("Edge browser found");
            return new EdgeExcelUtilities(driver, logger);
        } else {
            logger.info("Unsupported browser found");
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}