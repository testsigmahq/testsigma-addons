package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
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

    private static final String NEW_MODEL_STATE_SCRIPT =
            "return (function() {" +
                    "    try {" +
                    "        var app = document.querySelector('downloads-full-page-app');" +
                    "        if (!app || !app.shadowRoot) return null;" +
                    "        var list = app.shadowRoot.querySelector('downloads-list');" +
                    "        if (!list || !Array.isArray(list.downloads)) return null;" +
                    "        var downloads = [];" +
                    "        list.downloads.forEach(function(group) {" +
                    "            if (group && Array.isArray(group.downloads)) {" +
                    "                group.downloads.forEach(function(item) {" +
                    "                    if (item) downloads.push(item);" +
                    "                });" +
                    "            }" +
                    "        });" +
                    "        if (downloads.length === 0) return null;" +
                    "        return !downloads.some(function(item) {" +
                    "            var state = item && item.state ? String(item.state).toLowerCase() : '';" +
                    "            return state.indexOf('progress') !== -1;" +
                    "        });" +
                    "    } catch (e) {" +
                    "        return null;" +
                    "    }" +
                    "})();";

    private static final String NEW_MODEL_PATH_SCRIPT =
            "return (function() {" +
                    "    try {" +
                    "        var app = document.querySelector('downloads-full-page-app');" +
                    "        if (!app || !app.shadowRoot) return null;" +
                    "        var list = app.shadowRoot.querySelector('downloads-list');" +
                    "        if (!list || !Array.isArray(list.downloads) || list.downloads.length === 0) return null;" +
                    "        var group = list.downloads[0];" +
                    "        if (!group || !Array.isArray(group.downloads) || group.downloads.length === 0) return null;" +
                    "        var item = group.downloads[0];" +
                    "        if (!item || !item.icon) return null;" +
                    "        var match = String(item.icon).match(/path=([^&]*)/);" +
                    "        if (!match || !match[1]) return null;" +
                    "        return decodeURIComponent(match[1]).replace(/\\+/g, ' ');" +
                    "    } catch (e) {" +
                    "        return null;" +
                    "    }" +
                    "})();";

    private static final String LEGACY_PROGRESS_SCRIPT =
            "return (function() {" +
                    "    var progressValues = [];" +
                    "    var apps = document.querySelectorAll('downloads-app');" +
                    "    apps.forEach(function(app) {" +
                    "        if (!app.shadowRoot) return;" +
                    "        app.shadowRoot.querySelectorAll('fluent-progress-bar').forEach(function(bar) {" +
                    "            var value = bar.getAttribute('value');" +
                    "            if (value !== null) progressValues.push(value);" +
                    "        });" +
                    "    });" +
                    "    document.querySelectorAll('[role=\"progressbar\"]').forEach(function(bar) {" +
                    "        var value = bar.getAttribute('aria-valuenow');" +
                    "        if (value !== null) progressValues.push(value);" +
                    "    });" +
                    "    return progressValues;" +
                    "})();";

    private static final String LEGACY_PATH_SCRIPT =
            "return (function() {" +
                    "    try {" +
                    "        var items = Array.from(" +
                    "            document.querySelector('body > downloads-app')?.shadowRoot?.querySelectorAll('edge-card') || []" +
                    "        );" +
                    "        for (const item of items) {" +
                    "            var title = item.querySelector('.downloads_itemTitle');" +
                    "            if (!title || !title.textContent.trim()) continue;" +
                    "            var img = item.querySelector('.downloads_itemIconContainer > img');" +
                    "            var src = img ? img.getAttribute('src') : null;" +
                    "            if (!src) continue;" +
                    "            var decoded = decodeURIComponent(src);" +
                    "            var match = decoded.match(/path=(.*)&scale/);" +
                    "            if (match && match[1]) return match[1].replace(/\\+/g, ' ');" +
                    "        }" +

                    "        var legacyItems = document.querySelectorAll('div[role=\"listitem\"]');" +
                    "        for (const item of legacyItems) {" +
                    "            var button = item.querySelector('button[aria-label]');" +
                    "            if (!button || !button.getAttribute('aria-label')) continue;" +
                    "            var image = item.querySelector('img');" +
                    "            var imageSrc = image ? image.getAttribute('src') : null;" +
                    "            if (!imageSrc) continue;" +
                    "            return decodeURIComponent(imageSrc.split('path=')[1].split('&')[0])" +
                    "                .replace(/\\+/g, ' ');" +
                    "        }" +
                    "    } catch (e) {" +
                    "        return null;" +
                    "    }" +
                    "    return null;" +
                    "})();";

    @Override
    public File copyFileFromDownloads() throws Exception {
        String originalWindowHandle = driver.getWindowHandle();

        try {
            ((JavascriptExecutor) driver)
                    .executeScript("window.open('about:blank','_blank');");

            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(tabs.size() - 1));
            driver.get("edge://downloads/");

            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(60));

            // Handle timeout and show a meaningful error message
            try {
                wait.until((ExpectedCondition<Boolean>) driver -> isFileDownloaded());
            } catch (TimeoutException e) {
                throw new RuntimeException("File was not downloaded.");
            }

            String remoteFilePath = getDownloadedFileLocalPath();

            if (remoteFilePath == null || remoteFilePath.trim().isEmpty()) {
                throw new RuntimeException("File was not downloaded.");
            }

            logger.info("Downloaded file path=" + remoteFilePath);

            File downloadedFile =
                    super.createLocalFileFromDownloadsCopy(remoteFilePath);

            if (downloadedFile == null) {
                throw new RuntimeException(
                        "Unable to create a local copy of the downloaded file."
                );
            }

            return downloadedFile;

        } finally {
            try {
                driver.close();
                driver.switchTo().window(originalWindowHandle);
            } catch (Exception e) {
                logger.warn(
                        "Error closing downloads tab or switching back: "
                                + e.getMessage()
                );
            }
        }
    }

    @Override
    public boolean isFileDownloaded() {
        if (!Objects.requireNonNull(driver.getCurrentUrl())
                .startsWith("edge://downloads")) {
            driver.get("edge://downloads");
        }

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            Object newModelResult =
                    js.executeScript(NEW_MODEL_STATE_SCRIPT);

            if (newModelResult instanceof Boolean) {
                boolean complete = (Boolean) newModelResult;

                logger.info(
                        complete
                                ? "Download is complete."
                                : "Download is still in progress."
                );

                return complete;
            }

            @SuppressWarnings("unchecked")
            List<String> progressValues =
                    (List<String>) js.executeScript(LEGACY_PROGRESS_SCRIPT);

            if (progressValues != null && !progressValues.isEmpty()) {
                logger.info("Download is still in progress.");
                return false;
            }

            String legacyPath = getLegacyDownloadedFileLocalPath();

            if (legacyPath != null && !legacyPath.trim().isEmpty()) {
                logger.info("Download is complete.");
                return true;
            }

            logger.info("No downloaded file found.");
            return false;

        } catch (UnreachableBrowserException e) {
            logger.warn("Browser is unreachable: " + e.getMessage());
            return false;

        } catch (Exception e) {
            logger.warn("Error checking download status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDownloadedFileLocalPath() {
        driver.get("edge://downloads");

        logger.info("Switched to downloads page.");

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            Object newModelResult =
                    js.executeScript(NEW_MODEL_PATH_SCRIPT);

            if (newModelResult != null) {
                String path = newModelResult.toString();

                if (!path.trim().isEmpty()) {
                    logger.info("Found file path: " + path);
                    return path;
                }
            }

            String legacyPath = getLegacyDownloadedFileLocalPath();

            if (legacyPath != null && !legacyPath.trim().isEmpty()) {
                logger.info("Found file path: " + legacyPath);
                return legacyPath;
            }

            logger.warn("No downloaded file found in downloads page.");
            return null;

        } catch (Exception e) {
            logger.warn(
                    "Error retrieving the file path: "
                            + e.getMessage()
            );
            return null;
        }
    }

    private String getLegacyDownloadedFileLocalPath() {
        try {
            Object result =
                    ((JavascriptExecutor) driver)
                            .executeScript(LEGACY_PATH_SCRIPT);

            if (result != null) {
                String path = result.toString();

                if (!path.trim().isEmpty()) {
                    return path;
                }
            }

        } catch (Exception e) {
            logger.warn(
                    "Error retrieving legacy download path: "
                            + e.getMessage()
            );
        }

        return null;
    }
}