package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class PdfDocUtilitiesFactory {

    public static PdfDocUtilities create(WebDriver driver, Logger logger) {
        String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();

        if (browserName.contains("chrome")) {
            return new ChromePdfDocUtilities(driver, logger);
        } else if (browserName.contains("edge")) {
            return new EdgePdfDocUtilities(driver, logger);
        } else {
            throw new RuntimeException("Unsupported browser: " + browserName);
        }
    }
}