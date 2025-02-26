package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public class EdgePdfDocUtilities implements PdfDocUtilities {

    private WebDriver driver;
    private Logger logger;

    public EdgePdfDocUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public File copyFileFromDownloads(String fileFormat, String fileName) throws Exception {
        String url = driver.getCurrentUrl();
        File downloadedFile = null;
        try {
            driver.navigate().to("edge://downloads");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    return isFileDownloaded();
                }
            });
            String remoteFilePath = fileName != null ? getFilePathByFileNameInDownloads(fileName) : getDownloadedFileLocalPath();
            if (remoteFilePath != null) {
                logger.info("Downloaded file path=" + remoteFilePath);
                downloadedFile = createLocalFileFromDownloadsCopy(remoteFilePath, fileFormat);
            } else {
                throw new RuntimeException("File path not found.");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                Thread.sleep(3000);
                driver.navigate().to(url);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.warn("Thread was interrupted: " + ie.getMessage());
            }
        }
        return downloadedFile;
    }

    @Override
    public boolean isFileDownloaded() {
        if (!driver.getCurrentUrl().startsWith("edge://downloads")) {
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

    @Override
    public String getDownloadedFileLocalPath() {
        driver.get("edge://downloads");

        logger.warn("Switched to downloads page.");
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            String script = "return (function() {" +
                    "    let pdfPath = null;" +
                    "    try {" +
                    "        let downloadItems = Array.from(document.querySelector(\"body > downloads-app\")?.shadowRoot?.querySelectorAll(\"edge-card\") || []);" +
                    "        for (const downloadItem of downloadItems) {" +
                    "            const fileNameElement = downloadItem.querySelector(\".downloads_itemTitle\");" +
                    "            if (fileNameElement && fileNameElement.textContent.trim().toLowerCase().endsWith('.pdf')) {" +
                    "                const filePath = downloadItem.querySelector('.downloads_itemIconContainer > img')?.getAttribute('src');" +
                    "                if (filePath) {" +
                    "                    try {" +
                    "                        const decodedPath = decodeURIComponent(filePath);" +
                    "                        const match = decodedPath.match(/path=(.*)&scale/);" +
                    "                        if (match && match[1]) {" +
                    "                            pdfPath = match[1].replace(/\\+/g, ' ');" +
                    "                            return pdfPath;" +
                    "                        }" +
                    "                    } catch (decodeError) { console.error('Error decoding file path:', decodeError); }" +
                    "                }" +
                    "            }" +
                    "        }" +

                    "        let downloadItemsLegacy = document.querySelectorAll('div[role=\"listitem\"]');" +
                    "        for (const item of downloadItemsLegacy) {" +
                    "            let fileNameElement = item.querySelector('button[aria-label]');" +
                    "            if (fileNameElement) {" +
                    "                let fileName = fileNameElement.getAttribute('aria-label');" +
                    "                if (fileName && fileName.toLowerCase().endsWith('.pdf')) {" +
                    "                    let img = item.querySelector('img');" +
                    "                    if (img) {" +
                    "                        let src = img.getAttribute('src');" +
                    "                        if (src) {" +
                    "                            try {" +
                    "                                pdfPath = decodeURIComponent(src.split('path=')[1].split('&')[0]).replace(/\\+/g, ' ');" +
                    "                                return pdfPath;" +
                    "                            } catch (e) { console.error('Error extracting or decoding file path:', e); }" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    } catch (error) {" +
                    "        console.error('Error during PDF path retrieval:', error);" +
                    "    }" +
                    "    return pdfPath || null;" +
                    "})();";

            Object result = js.executeScript(script);

            if (result != null) {
                String resultString = result.toString();
                logger.info("Found PDF file path: " + resultString);
                return resultString;
            } else {
                logger.warn("No PDF file found in downloads or an error occurred.");
                return null;
            }
        } catch (Exception e) {
            logger.warn("Error retrieving the file path: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getFilePathByFileNameInDownloads(String desiredFileName) {
        try {
            String script = "return (function(targetFileName) {" +
                    "    let pdfPath = null;" +
                    "    try {" +
                    "        let downloadItems = Array.from(document.querySelector(\"body > downloads-app\")?.shadowRoot?.querySelectorAll(\"edge-card\") || []);" +
                    "        for (const downloadItem of downloadItems) {" +
                    "            const fileNameElement = downloadItem.querySelector(\".downloads_itemTitle\");" +
                    "            if (fileNameElement) {" +
                    "                const fileName = fileNameElement.textContent.trim();" +
                    "                if (fileName.toLowerCase() === targetFileName.toLowerCase() && fileName.toLowerCase().endsWith('.pdf')) {" +
                    "                    const filePath = downloadItem.querySelector('.downloads_itemIconContainer > img')?.getAttribute('src');" +
                    "                    if (filePath) {" +
                    "                        try {" +
                    "                            const decodedPath = decodeURIComponent(filePath);" +
                    "                            const match = decodedPath.match(/path=(.*)&scale/);" +
                    "                            if (match && match[1]) {" +
                    "                                pdfPath = match[1].replace(/\\+/g, ' ');" +
                    "                                pdfPath = pdfPath.replace(/%20/g, ' ');" +
                    "                                return pdfPath;" +
                    "                            }" +
                    "                        } catch (decodeError) {" +
                    "                            console.error('Error decoding file path:', decodeError);" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "        let downloadItemsLegacy = document.querySelectorAll('div[role=\"listitem\"]');" +
                    "        for (const item of downloadItemsLegacy) {" +
                    "            let fileNameElement = item.querySelector('button[aria-label]');" +
                    "            if (fileNameElement) {" +
                    "                let fileName = fileNameElement.getAttribute('aria-label');" +
                    "                if (fileName && fileName.toLowerCase() === targetFileName.toLowerCase() && fileName.toLowerCase().endsWith('.pdf')) {" +
                    "                    let img = item.querySelector('img');" +
                    "                    if (img) {" +
                    "                        let src = img.getAttribute('src');" +
                    "                        if (src) {" +
                    "                            try {" +
                    "                                pdfPath = decodeURIComponent(src.split('path=')[1].split('&')[0]).replace(/\\+/g, ' ');" +
                    "                                pdfPath = pdfPath.replace(/%20/g, ' '); " +
                    "                                return pdfPath;" +
                    "                            } catch (e) {" +
                    "                                console.error('Error extracting or decoding file path:', e);" +
                    "                            }" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    } catch (error) {" +
                    "        console.error('Error during PDF path retrieval:', error);" +
                    "    }" +
                    "    return pdfPath || null;" +
                    "})('" + desiredFileName + "');";

            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object obj = js.executeScript(script);

            if (obj == null) {
                logger.info("There is no file with name: " + desiredFileName);
                return null;
            }

            return obj.toString();
        } catch (Exception e) {
            logger.warn("Error occurred: " + e.getMessage());
            throw new RuntimeException("Error occurred while retrieving file path", e);
        }
    }

    private File createLocalFileFromDownloadsCopy(String path, String fileFormat) throws Exception {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement elem = (WebElement) js.executeScript(
                    "var input = window.document.createElement('INPUT'); " +
                            "input.setAttribute('type', 'file'); " +
                            "input.style.display = 'none'; " +
                            "window.document.body.appendChild(input); " +
                            "return input;"
            );

            elem.sendKeys(path);

            Object result = js.executeAsyncScript(
                    "var input = arguments[0], callback = arguments[1]; " +
                            "var reader = new FileReader(); " +
                            "reader.onload = function (ev) { callback(reader.result); }; " +
                            "reader.onerror = function (ex) { callback(ex.message); }; " +
                            "reader.readAsDataURL(input.files[0]); " +
                            "input.remove();",
                    elem
            );

            if (result == null || !result.toString().startsWith("data:")) {
                throw new RuntimeException("Failed to get file content: " + result);
            }

            String base64String = result.toString().substring(result.toString().indexOf("base64,") + 7);
            File f = new File(path);
            String fileName = f.getName();
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            File downloadedFile = File.createTempFile(fileName, "." + fileFormat);
            Files.write(Paths.get(downloadedFile.getAbsolutePath()), decodedBytes);
            logger.info("Local path: " + downloadedFile.getAbsolutePath());
            return downloadedFile;

        } catch (Exception e) {
            logger.warn("Error creating local file from downloads copy: " + e.getMessage());
            throw e;
        }
    }
}