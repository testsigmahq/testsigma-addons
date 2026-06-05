package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ExcelUtilitiesFactory {

    public static ExcelUtilities create(WebDriver driver, Logger logger) {
        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
        String browserName = caps.getBrowserName();
        if (browserName == null) browserName = "";
        browserName = browserName.toLowerCase();
        logger.info("browserName: " + browserName);

        if (browserName.contains("chrome")) {
            logger.info("Chrome browser found");
            return new ChromeExcelUtilities(driver, logger);
        } else if (browserName.contains("edge") || browserName.contains("microsoftedge")) {
            logger.info("Edge browser found");
            return new EdgeExcelUtilities(driver, logger);
        }

        // browserName is empty in some cloud/remote environments — fall back to navigator.userAgent
        logger.info("browserName empty or unrecognized, checking capabilities: " + caps.asMap().keySet());
        try {
            String userAgent = (String) ((JavascriptExecutor) driver)
                    .executeScript("return navigator.userAgent;");
            logger.info("userAgent: " + userAgent);
            if (userAgent != null) {
                String ua = userAgent.toLowerCase();
                // Edge must be checked before Chrome because Edge UA also contains "chrome"
                if (ua.contains("edg/") || ua.contains("edga/") || ua.contains("edgios/")) {
                    logger.info("Edge browser detected via userAgent");
                    return new EdgeExcelUtilities(driver, logger);
                } else if (ua.contains("chrome")) {
                    logger.info("Chrome browser detected via userAgent");
                    return new ChromeExcelUtilities(driver, logger);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to read userAgent: " + e.getMessage());
        }

        logger.info("Unsupported browser found");
        throw new RuntimeException("Unsupported browser. browserName='" + browserName
                + "', capabilities=" + caps.asMap().keySet());
    }
}