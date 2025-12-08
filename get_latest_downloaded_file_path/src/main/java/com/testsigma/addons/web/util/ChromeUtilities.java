package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class ChromeUtilities extends BaseUtilities {

    public ChromeUtilities(WebDriver driver, Logger logger) {
        super(driver, logger);
    }

    @Override
    public File copyFileFromDownloads() throws Exception {
        String originalWindowHandle = driver.getWindowHandle();
        try {
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get("chrome://downloads/");

            if (!isFileDownloaded()) {
                throw new RuntimeException("File is still downloading.");
            }
            String remoteFilePath = getDownloadedFileLocalPath();
            if (remoteFilePath != null) {
                File localFile = createLocalFileFromDownloadsCopy(remoteFilePath);
                return localFile;
            } else {
                throw new RuntimeException("File path not found.");
            }
        } finally {
            try {
                driver.close();
                driver.switchTo().window(originalWindowHandle);
            } catch (Exception e) {
                logger.warn("Error closing downloads tab or switching back: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isFileDownloaded() {
        if (!Objects.requireNonNull(driver.getCurrentUrl()).startsWith("chrome://downloads")) {
            driver.get("chrome://downloads/");
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object obj = js.executeScript(
                "return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList')" +
                        ".items.filter(e => e.state === 'IN_PROGRESS').map(e => e.filePath || e.file_path || e.fileUrl || e.file_url); ");
        return !(obj instanceof List) || ((List<?>) obj).isEmpty();
    }

    @Override
    public String getDownloadedFileLocalPath() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object obj = js.executeScript(
                    "return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items[0].filePath; ");
            assert obj != null;
            return obj.toString();
        } catch (Exception e) {
            logger.info("No files in the downloads");
            throw new RuntimeException("No files in the downloads");
        }
    }
}