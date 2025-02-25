package com.testsigma.addons.web;

import com.testsigma.addons.web.utils.BrowserUtilities;
import com.testsigma.addons.web.utils.BrowserUtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Action(actionText = "Wait until all files are download in all browsers",
        description = "Waits until all files are download in all browsers.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class WaitUntilDownloadIsCompleteForAllBrowsers extends WebAction {

    private static final String SUCCESS_MESSAGE = "Download is completed";
    private static final String FAILURE_MESSAGE = "Download is not yet completed. Waited for <b>\"%s\"</b> seconds for download to complete";

    @Override
    public Result execute() {
        Result result = Result.FAILED;
        String currentURL = driver.getCurrentUrl();
        BrowserUtilities utilities = BrowserUtilitiesFactory.create(driver, logger);

        String currentWindowHandle = driver.getWindowHandle();
        utilities.openNewTabAndNavigateToDownloads();
        logger.info("Opened new tab");

        try {
            WebDriverWait waiter = new WebDriverWait(driver, Duration.ofSeconds(600), Duration.ofSeconds(5));
            logger.info("Waiting for download to complete");
            boolean isDownloadCompleted = waiter.until(webDriver -> utilities.isDownloadComplete());
            if (!isDownloadCompleted) {
                throw new TimeoutException("Download did not complete within the allotted time.");
            }
            logger.info("Success message: " + SUCCESS_MESSAGE);
            setSuccessMessage(SUCCESS_MESSAGE);
            result = Result.SUCCESS;
        } catch (TimeoutException e) {
            setErrorMessage(String.format(FAILURE_MESSAGE, 600) + ". Error: " + e.getMessage());
        } finally {
            logger.info("Driver's current URL after download is complete: " + driver.getCurrentUrl());
            if (driver.getCurrentUrl().contains("downloads")) {
                try {
                    String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();
                    if(browserName.contains("edge")) {
                        driver.navigate().to(currentURL);
                        Thread.sleep(3000);
                        logger.info("Navigated back to current URL.");
                    } else {
                        driver.switchTo().window(currentWindowHandle);
                        Thread.sleep(3000);
                        logger.info("Switched back to current window.");
                    }
                } catch (NoSuchWindowException e) {
                    logger.warn("Exception while navigating back from downloads page, current URL is " + driver.getCurrentUrl() + " and exception " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }
}