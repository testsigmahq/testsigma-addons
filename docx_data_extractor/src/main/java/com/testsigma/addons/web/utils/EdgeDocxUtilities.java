package com.testsigma.addons.web.utils;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;

public class EdgeDocxUtilities implements DocxDocUtilities {

    private WebDriver driver;
    private Logger logger;

    public EdgeDocxUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public File copyFileFromDownloads(String fileFormat, String fileName) throws Exception {
        String currentWindowHandle = driver.getWindowHandle();
        File downloadedFile = null;
        try {
            driver.navigate().to("edge://downloads/all");
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
            driver.switchTo().window(currentWindowHandle);
        }
        return downloadedFile;
    }

    @Override
    public boolean isFileDownloaded() {
        if (!driver.getCurrentUrl().startsWith("edge://downloads/all")) {
            driver.get("edge://downloads/all");
        }

        String jsFunction = "return (function() {" +
                "    const downloadItems = document.querySelectorAll('div[role=\"listitem\"]');" +
                "    let inProgress = false;" +
                "    downloadItems.forEach(item => {" +
                "        const showInFinderButton = Array.from(item.querySelectorAll('button')).find(btn => {" +
                "            const span = btn.querySelector('span');" +
                "            return span && span.textContent === 'Show in Finder';" +
                "        });" +
                "        if (!showInFinderButton) {" +
                "            const actionButton = item.querySelector('button[id^=\"resume\"], button[id^=\"pause\"]');" +
                "            if (actionButton) {" +
                "                const buttonText = actionButton.querySelector('span').textContent;" +
                "                if (buttonText === 'Pause' || buttonText === 'Resume') {" +
                "                    inProgress = true;" +
                "                }" +
                "            }" +
                "        }" +
                "    });" +
                "    if (inProgress) {" +
                "        return 'IN_PROGRESS';" +
                "    } else {" +
                "        return 'COMPLETED';" +
                "    }" +
                "})();";

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String result = (String) js.executeScript(jsFunction);

        if ("IN_PROGRESS".equals(result)) {
            logger.info("At least one file is in progress");
            return false;
        } else if ("COMPLETED".equals(result)) {
            logger.info("All files are downloaded");
            return true;
        }

        logger.warn("Unexpected result from script execution");
        return false;
    }

    @Override
    public String getDownloadedFileLocalPath() {
        driver.get("edge://downloads/all");

        logger.warn("Switched");
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Updated JavaScript to find and return the first PDF file path
            String script = "return (function() {" +
                    "    let filePaths = [];" +
                    "    let downloadItems = document.querySelectorAll('div[role=\"listitem\"]');" +
                    "    downloadItems.forEach(item => {" +
                    "        let fileNameElement = item.querySelector('button[aria-label]');" +
                    "        if (fileNameElement) {" +
                    "            let fileName = fileNameElement.getAttribute('aria-label');" +
                    "            if (fileName && fileName.toLowerCase().endsWith('.docx')) {" +
                    "                let img = item.querySelector('img');" +
                    "                if (img) {" +
                    "                    let src = img.getAttribute('src');" +
                    "                    if (src) {" +
                    "                        try {" +
                    "                            let filePath = decodeURIComponent(src.split('path=')[1].split('&')[0]).replace(/\\+/g, ' ');" +
                    "                            filePaths.push(filePath);" +
                    "                        } catch (e) {" +
                    "                            console.error('Error decoding src:', e);" +
                    "                        }" +
                    "                    }" +
                    "                }" +
                    "            }" +
                    "        }" +
                    "    });" +
                    "    return filePaths.length > 0 ? filePaths[0] : 'No DOCX file found';" +
                    "})();";

            // Execute the JavaScript
            Object result = js.executeScript(script);

            // Process the result
            if (result != null) {
                String resultString = result.toString();
                if ("No DOCX file found".equals(resultString)) {
                    logger.warn("No DOCX file found in the downloads page.");
                    return null;
                }
                return resultString;
            } else {
                logger.warn("Result is null.");
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
            String script = "const desiredFileName = '" + desiredFileName + "';" +
                    "const result = Array.from(document.querySelectorAll('img'))" +
                    ".map(img => {" +
                    "    let ariaLabel = img.getAttribute('aria-label');" +
                    "    let src = img.getAttribute('src');" +
                    "    if (ariaLabel && ariaLabel.endsWith('.docx')) {" +
                    "        let filePath = src ? decodeURIComponent(src.split('path=')[1].split('&')[0]) : null;" +
                    "        if (filePath) {" +
                    "            filePath = filePath.replace(/\\+/g, ' ');" +
                    "        }" +
                    "        return { file: ariaLabel, path: filePath };" +
                    "    }" +
                    "})" +
                    ".filter(item => item)" +
                    ".find(item => item.file.includes(desiredFileName));" +
                    "const filePath = result ? result.path : null;" +
                    "return filePath;";

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
                            "reader.readAsDataURL(input.files[0]); "
                            + "input.remove();",
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