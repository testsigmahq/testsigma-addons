package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DownloadUtilities {

    WebDriver driver;
    Logger logger;

    public DownloadUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }


    private Boolean checkFileDownloadStatus(final String expectedFileName) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        logger.info("initiating js Executor");

        try {
            String filepath;
            String script = "return Array.from(document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items)" +
                    ".find(item => item.fileName && item.fileName.includes('" + expectedFileName + "')" +
                    " && (item.state === 2 || item.state === 'complete'))";
            logger.info("Executing script: " + script);
            Object obj2 = js.executeScript(script);

            // Check if obj2 is not null before casting
            filepath = obj2 != null ? obj2.toString() : null;

            if (filepath != null) {
                logger.info("File path found: " + filepath);
                return true;
            } else {
                System.out.println("File path not found.");
                return false;
            }
        } catch (Exception e) {
            logger.info("Exception occurred : " + e.getMessage());
            throw new RuntimeException("File not Found");
        }
    }

    public Boolean SearchForTheFile(final String fileNameToBeSearched) {
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get("chrome://downloads/");
        logger.info("Got driver url: " + driver.getCurrentUrl());
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            wait.pollingEvery(Duration.ofMillis(500)); // Fluent wait

            return wait.until((ExpectedCondition<Boolean>) d -> checkFileDownloadStatus(fileNameToBeSearched));
        } catch (TimeoutException e) {
            logger.warn(ExceptionUtils.getMessage(e));
            throw new RuntimeException("The file was not found");
        } catch (Exception e) {
            logger.info("Exception occurred : " + e.getMessage());
            throw new RuntimeException("Exception occurred : " + ExceptionUtils.getMessage(e));
        }
    }

}
