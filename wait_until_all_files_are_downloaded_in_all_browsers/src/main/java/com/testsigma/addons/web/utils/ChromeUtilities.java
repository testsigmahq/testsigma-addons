package com.testsigma.addons.web.utils;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChromeUtilities implements BrowserUtilities {
    private WebDriver driver;
    private Logger logger;

    public ChromeUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public void openNewTabAndNavigateToDownloads() {
        ((JavascriptExecutor) driver).executeScript("window.open()");
        Set<String> allWindows = driver.getWindowHandles();
        ArrayList<String> tabs = new ArrayList<>(allWindows);
        driver.switchTo().window(tabs.get(tabs.size() - 1));
        driver.navigate().to("chrome://downloads/");
        logger.info("Navigated to chrome://downloads/");
    }

    @Override
    public boolean isDownloadComplete() {
        try {
            String chromeJavaScript = "var tag = document.querySelector('downloads-manager').shadowRoot;" +
                    "    var item_tags = tag.querySelectorAll('downloads-item');" +
                    "    var item_tags_length = item_tags.length;" +
                    "    var progress_lst = [];" +
                    "    for(var i=0; i<item_tags_length; i++) {" +
                    "        var intag = item_tags[i].shadowRoot;" +
                    "        var progress_tag = intag.getElementById('progress');" +
                    "        var progress = null;" +
                    "        if(progress_tag && progress_tag.value < 100) {" +
                    "             progress = progress_tag.value;" +
                    "        }" +
                    "        if(progress!=null) progress_lst.push(progress);" +
                    "    }" +
                    "    return progress_lst";
            List<WebElement> webElements = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(chromeJavaScript);
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