package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class ChromePdfDocUtilities implements PdfDocUtilities {

    private WebDriver driver;
    private Logger logger;

    public ChromePdfDocUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public File copyFileFromDownloads(String fileFormat, String fileName) throws Exception {
        driver.navigate().to("chrome://downloads/");
        if (!isFileDownloaded()) {
            throw new RuntimeException("File is still downloading.");
        }
        String remoteFilePath = fileName != null ? getFilePathByFileNameInDownloads(fileName) : getDownloadedFileLocalPath();
        if (remoteFilePath != null) {
            return createLocalFileFromDownloadsCopy(remoteFilePath, fileFormat);
        } else {
            throw new RuntimeException("File path not found.");
        }
    }

    @Override
    public boolean isFileDownloaded() {
        if (!driver.getCurrentUrl().startsWith("chrome://downloads")) {
            driver.get("chrome://downloads/");
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList')" +
                ".items.filter(e => e.state === 'IN_PROGRESS').map(e => e.filePath || e.file_path || e.fileUrl || e.file_url); ");
        if (obj != null && obj instanceof List && !((List) obj).isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String getDownloadedFileLocalPath() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items[0].filePath; ");
            return obj.toString();
        } catch (Exception e){
            logger.info("No files in the downloads");
            throw new RuntimeException("No files in the downloads");
        }
    }

    @Override
    public String getFilePathByFileNameInDownloads(String desiredFileName) {
        try {
            String script = "return Array.from(document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items)" +
                    "    .find(item => item.fileName && item.fileName.includes('" + desiredFileName + "')).filePath;";
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object obj = js.executeScript(script);
            return obj.toString();
        } catch(Exception e){
            logger.info("There is no file with name:"+desiredFileName);
            throw new RuntimeException("There is no file with name:"+desiredFileName);
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