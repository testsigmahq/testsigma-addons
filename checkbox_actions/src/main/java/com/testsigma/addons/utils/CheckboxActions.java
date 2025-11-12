package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CheckboxActions {

    public void checkAllEnabledCheckboxes(WebDriver driver, Logger logger) {
        toggleAllEnabledCheckboxes(driver, logger, true);
    }

    public void unCheckAllEnabledCheckboxes(WebDriver driver, Logger logger) {
        toggleAllEnabledCheckboxes(driver, logger, false);
    }

    private void toggleAllEnabledCheckboxes(WebDriver driver, Logger logger, boolean check) {
        try {
            String platform = getPlatformType(driver);
            logger.info("Starting to " + (check ? "check" : "uncheck") + " all enabled checkboxes on " + platform + " platform");
            
            // Find all checkboxes based on platform
            List<WebElement> checkboxes = findCheckboxes(driver, platform);
            logger.info("Total checkboxes found: " + checkboxes.size());

            if (checkboxes.isEmpty()) {
                logger.info("No checkboxes found on the current page");
                return;
            }

            int processedCount = 0;
            int skippedCount = 0;

            for (WebElement checkbox : checkboxes) {
                try {
                    // Skip disabled checkboxes
                    if (!checkbox.isEnabled()) {
                        logger.info("Skipping disabled checkbox");
                        skippedCount++;
                        continue;
                    }

                    String checkboxInfo = getCheckboxInfo(checkbox, platform);
                    logger.info("Processing enabled checkbox: " + checkboxInfo);

                    // Click only if current state does not match desired state
                    if (checkbox.isSelected() != check) {
                        checkbox.click();
                        logger.info((check ? "Checked" : "Unchecked") + " checkbox: " + checkboxInfo);
                        processedCount++;
                    } else {
                        logger.info("Checkbox already in desired state: " + checkboxInfo);
                        skippedCount++;
                    }
                } catch (Exception e) {
                    logger.info("Error processing checkbox: " + e.getMessage());
                    skippedCount++;
                }
            }

            logger.info("Checkbox processing completed. Processed: " + processedCount + ", Skipped: " + skippedCount);
            
        } catch (Exception e) {
            logger.info("Error in toggleAllEnabledCheckboxes: " + e.getMessage());
            throw new RuntimeException("Failed to " + (check ? "check" : "uncheck") + " checkboxes: " + e.getMessage(), e);
        }
    }

    private String getPlatformType(WebDriver driver) {
        if (driver instanceof AndroidDriver) {
            return "Android";
        } else if (driver instanceof IOSDriver) {
            return "iOS";
        } else {
            return "Web";
        }
    }

    private List<WebElement> findCheckboxes(WebDriver driver, String platform) {
        switch (platform) {
            case "Android":
                // Android-specific checkbox locators
                return driver.findElements(By.xpath("//android.widget.CheckBox | //android.widget.Switch | //input[@type='checkbox']"));
            case "iOS":
                // iOS-specific checkbox locators
                return driver.findElements(By.xpath("//XCUIElementTypeSwitch | //XCUIElementTypeButton[@type='XCUIElementTypeButton'] | //input[@type='checkbox']"));
            default:
                // Web checkbox locators
                return driver.findElements(By.xpath("//input[@type='checkbox']"));
        }
    }

    private String getCheckboxInfo(WebElement checkbox, String platform) {
        try {
            switch (platform) {
                case "Android":
                    String androidText = checkbox.getAttribute("text");
                    String androidContentDesc = checkbox.getAttribute("content-desc");
                    String androidResourceId = checkbox.getAttribute("resource-id");
                    return String.format("Android[text='%s', content-desc='%s', resource-id='%s']", 
                        androidText, androidContentDesc, androidResourceId);
                case "iOS":
                    String iosLabel = checkbox.getAttribute("label");
                    String iosName = checkbox.getAttribute("name");
                    String iosValue = checkbox.getAttribute("value");
                    return String.format("iOS[label='%s', name='%s', value='%s']", 
                        iosLabel, iosName, iosValue);
                default:
                    String webName = checkbox.getAttribute("name");
                    String webId = checkbox.getAttribute("id");
                    String webValue = checkbox.getAttribute("value");
                    return String.format("Web[name='%s', id='%s', value='%s']", 
                        webName, webId, webValue);
            }
        } catch (Exception e) {
            return "Unknown checkbox";
        }
    }
}
