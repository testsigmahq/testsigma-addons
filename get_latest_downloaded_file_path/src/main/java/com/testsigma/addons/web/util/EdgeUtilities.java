package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EdgeUtilities extends BaseUtilities {

    public EdgeUtilities(WebDriver driver, Logger logger) {
        super(driver, logger);
    }

    @Override
    public File copyFileFromDownloads() throws Exception {
        String originalWindowHandle = driver.getWindowHandle();
        File downloadedFile = null;
        try {
            // Create a new tab with JS
            ((JavascriptExecutor) driver).executeScript("window.open('about:blank','_blank');");

            // Switch to the new tab
            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(tabs.size() - 1));

            // Go to edge://downloads/
            driver.get("edge://downloads/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            wait.until((ExpectedCondition<Boolean>) driver -> isFileDownloaded());
            String remoteFilePath = getDownloadedFileLocalPath();
            if (remoteFilePath != null) {
                logger.info("Downloaded file path=" + remoteFilePath);
                downloadedFile = super.createLocalFileFromDownloadsCopy(remoteFilePath);
            } else {
                throw new RuntimeException("File path not found.");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                driver.close();
                driver.switchTo().window(originalWindowHandle);
            } catch (Exception e) {
                logger.warn("Error closing downloads tab or switching back: " + e.getMessage());
            }
        }
        return downloadedFile;
    }

    @Override
    public boolean isFileDownloaded() {
        if (!Objects.requireNonNull(driver.getCurrentUrl()).startsWith("edge://downloads")) {
            driver.get("edge://downloads");
        }

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
            List<WebElement> progressValues = (List<WebElement>) ((JavascriptExecutor) driver)
                    .executeScript(edgeJavaScript);
            if (progressValues != null && !progressValues.isEmpty()) {
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

    @Override
    public String getDownloadedFileLocalPath() {
        driver.get("edge://downloads");

        logger.warn("Switched to downloads page.");
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            String script = "return (function() {" +
                    "    let pdfPath = null;" +
                    "    try {" +
                    "        let downloadItems = Array.from(document.querySelector(\"body > downloads-app\")?.shadowRoot?.querySelectorAll(\"edge-card\") || []);"
                    +
                    "        for (const downloadItem of downloadItems) {" +
                    "            const fileNameElement = downloadItem.querySelector(\".downloads_itemTitle\");" +
                    "            if (fileNameElement && fileNameElement.textContent.trim().length > 0) {" +
                    "                const filePath = downloadItem.querySelector('.downloads_itemIconContainer > img')?.getAttribute('src');"
                    +
                    "                if (filePath) {" +
                    "                    try {" +
                    "                        const decodedPath = decodeURIComponent(filePath);" +
                    "                        const match = decodedPath.match(/path=(.*)&scale/);" +
                    "                        if (match && match[1]) {" +
                    "                            pdfPath = match[1].replace(/\\+/g, ' ');" +
                    "                            return pdfPath;" +
                    "                        }" +
                    "                    } catch (decodeError) { console.error('Error decoding file path:', decodeError); }"
                    +
                    "                }" +
                    "            }" +
                    "        }" +

                    "        let downloadItemsLegacy = document.querySelectorAll('div[role=\"listitem\"]');" +
                    "        for (const item of downloadItemsLegacy) {" +
                    "            let fileNameElement = item.querySelector('button[aria-label]');" +
                    "            if (fileNameElement) {" +
                    "                let fileName = fileNameElement.getAttribute('aria-label');" +
                    "                if (fileName && fileName.length > 0) {" +
                    "                    let img = item.querySelector('img');" +
                    "                    if (img) {" +
                    "                        let src = img.getAttribute('src');" +
                    "                        if (src) {" +
                    "                            try {" +
                    "                                pdfPath = decodeURIComponent(src.split('path=')[1].split('&')[0]).replace(/\\+/g, ' ');"
                    +
                    "                                return pdfPath;" +
                    "                            } catch (e) { console.error('Error extracting or decoding file path:', e); }"
                    +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    } catch (error) {" +
                    "        console.error('Error during PDF path retrieval:', error);" +
                    "    }" +
                    "    return null;" +
                    "})();";

            Object result = js.executeScript(script);

            if (result != null) {
                String resultString = result.toString();
                logger.info("Found file path: " + resultString);
                return resultString;
            } else {
                logger.warn("No file found in downloads or an error occurred.");
                return null;
            }
        } catch (Exception e) {
            logger.warn("Error retrieving the file path: " + e.getMessage());
            return null;
        }
    }
}