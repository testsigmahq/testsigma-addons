package com.testsigma.addons.web.utils;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.List;

public class EdgeUtilities implements BrowserUtilities {
    private WebDriver driver;
    private Logger logger;

    public EdgeUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public void openNewTabAndNavigateToDownloads() {
        driver.switchTo().newWindow(WindowType.TAB);
        driver.navigate().to("edge://downloads");
        logger.info("Navigated to edge://downloads");
    }

    @Override
    public boolean isDownloadComplete() {
        try {
            String edgeJavaScript = "function getProgressValues() {" +
                    "  let progressValues = [];" +
                    "  const shadowRoots = document.querySelectorAll('downloads-app');" +
                    "  if (shadowRoots.length > 0) {" +
                    "    shadowRoots.forEach(shadowRoot => {" +
                    "      const progressBarElements = shadowRoot.shadowRoot.querySelectorAll('fluent-progress-bar');" +
                    "      progressBarElements.forEach(progressBar => {" +
                    "        const progressValue = progressBar.getAttribute('value');" +
                    "        if (progressValue !== null) {" +
                    "          progressValues.push(progressValue);" +
                    "        }" +
                    "      });" +
                    "    });" +
                    "  }" +
                    "  const progressBarElements = document.querySelectorAll('[role=\"progressbar\"]');" +
                    "  progressBarElements.forEach(progressBar => {" +
                    "    const progressValue = progressBar.getAttribute('aria-valuenow');" +
                    "    if (progressValue !== null) {" +
                    "      progressValues.push(progressValue);" +
                    "    }" +
                    "  });" +
                    "  return progressValues;" +
                    "}" +
                    "return getProgressValues();";
            List<WebElement> progressValues = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(edgeJavaScript);
            if (progressValues != null && progressValues.size() > 0) {
                logger.info("Download is still in progress.");
                return false;
            }
            logger.info("Download is complete.");
            return true;
        } catch (UnreachableBrowserException e) {
            logger.warn("Browser is unreachable: " + e.getMessage());
            return false;
        }
    }
}