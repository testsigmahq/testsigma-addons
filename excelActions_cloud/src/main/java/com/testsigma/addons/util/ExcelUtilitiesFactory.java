package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ExcelUtilitiesFactory {

    public static ExcelUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();
        System.out.println("browserName: " + browserName);

        if (browserName.contains("chrome")) {
            return new ChromeExcelUtilities(driver, logger);
        } else if (browserName.contains("edge")) {
            return new EdgeExcelUtilities(driver, logger);
        } else {
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}