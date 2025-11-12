package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.StringJoiner;

public class CheckBoxLabelFetcher {

    public String getAllCheckboxLabels(WebDriver driver, Logger logger) {
        try {
            String platform = getPlatformType(driver);
            logger.info("Starting to fetch checkbox labels on " + platform + " platform");
            
            List<WebElement> checkboxes = findCheckboxes(driver, platform);
            logger.info("Checkboxes found: " + checkboxes.size());

            if (checkboxes.isEmpty()) {
                logger.info("No checkboxes found on the current page");
                return "";
            }

            StringJoiner labels = new StringJoiner(", ");
            int processedCount = 0;

            for (WebElement checkbox : checkboxes) {
                try {
                    logger.info("Finding label text for checkbox: " + getCheckboxInfo(checkbox, platform));

                    String labelText = getCheckboxLabel(checkbox, driver, platform, logger);

                    if (!labelText.isEmpty()) {
                        labels.add(labelText);
                        processedCount++;
                        logger.info("Found label: " + labelText);
                    } else {
                        logger.info("No label found for checkbox");
                    }
                } catch (Exception e) {
                    logger.info("Error processing checkbox for label: " + e.getMessage());
                }
            }

            String result = labels.toString();
            logger.info("Successfully processed " + processedCount + " checkbox labels. Result: " + result);
            return result;
            
        } catch (Exception e) {
            logger.info("Error in getAllCheckboxLabels: " + e.getMessage());
            throw new RuntimeException("Failed to fetch checkbox labels: " + e.getMessage(), e);
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

    private String getCheckboxLabel(WebElement checkbox, WebDriver driver, String platform, Logger logger) {
        try {
            switch (platform) {
                case "Android":
                    return getAndroidCheckboxLabel(checkbox);
                case "iOS":
                    return getIOSCheckboxLabel(checkbox);
                default:
                    return getWebCheckboxLabel(checkbox, driver);
            }
        } catch (Exception e) {
            logger.info("Error getting checkbox label: " + e.getMessage());
            return "";
        }
    }

    private String getAndroidCheckboxLabel(WebElement checkbox) {
        // Try text attribute first
        String text = checkbox.getAttribute("text");
        if (text != null && !text.trim().isEmpty()) {
            return text.trim();
        }

        // Try content-desc attribute
        String contentDesc = checkbox.getAttribute("content-desc");
        if (contentDesc != null && !contentDesc.trim().isEmpty()) {
            return contentDesc.trim();
        }

        // Try resource-id as fallback
        String resourceId = checkbox.getAttribute("resource-id");
        if (resourceId != null && !resourceId.trim().isEmpty()) {
            return resourceId.substring(resourceId.lastIndexOf("/") + 1);
        }

        return "";
    }

    private String getIOSCheckboxLabel(WebElement checkbox) {
        // Try label attribute first
        String label = checkbox.getAttribute("label");
        if (label != null && !label.trim().isEmpty()) {
            return label.trim();
        }

        // Try name attribute
        String name = checkbox.getAttribute("name");
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }

        // Try value attribute
        String value = checkbox.getAttribute("value");
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return "";
    }

    private String getWebCheckboxLabel(WebElement checkbox, WebDriver driver) {
        String labelText = "";

        // Try parent <label> wrapping the input first
        try {
            WebElement parentLabel = checkbox.findElement(By.xpath("ancestor::label"));
            labelText = parentLabel.getText().trim();
        } catch (Exception ignored) { }

        // Fallback to <label for="id"> association if ancestor label not found
        if (labelText.isEmpty()) {
            String id = checkbox.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                try {
                    WebElement label = driver.findElement(By.xpath("//label[@for='" + id + "']"));
                    labelText = label.getText().trim();
                } catch (Exception ignored) { }
            }
        }

        return labelText;
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

