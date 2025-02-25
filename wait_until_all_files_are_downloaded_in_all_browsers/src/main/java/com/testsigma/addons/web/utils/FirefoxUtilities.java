package com.testsigma.addons.web.utils;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FirefoxUtilities implements BrowserUtilities {
    private WebDriver driver;
    private Logger logger;

    public FirefoxUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public void openNewTabAndNavigateToDownloads() {
        ((JavascriptExecutor) driver).executeScript("window.open()");
        Set<String> allWindows = driver.getWindowHandles();
        ArrayList<String> tabs = new ArrayList<>(allWindows);
        driver.switchTo().window(tabs.get(tabs.size() - 1));
        driver.navigate().to("about:downloads");
        logger.info("Navigated to about:downloads");
    }

    @Override
    public boolean isDownloadComplete() {
        try {
            String firefoxJavaScript = "var downloadsListBox = document.querySelector('richlistbox.allDownloadsListBox');" +
                    "var items = downloadsListBox.querySelectorAll('richlistitem.download');" +
                    "var progress_lst = [];" +
                    "items.forEach(item => {" +
                    "  var progress = item.querySelector('progress.downloadProgress');" +
                    "  if (progress && progress.getAttribute('value') < 100) {" +
                    "    progress_lst.push(progress.getAttribute('value'));" +
                    "  }" +
                    "});" +
                    "return progress_lst;";
            List<WebElement> webElements = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(firefoxJavaScript);
            if (webElements != null && webElements.size() > 0) {
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