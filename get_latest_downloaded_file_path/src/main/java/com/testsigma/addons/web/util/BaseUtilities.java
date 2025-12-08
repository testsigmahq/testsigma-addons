package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public abstract class BaseUtilities implements Utilities {

    protected WebDriver driver;
    protected Logger logger;

    public BaseUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    protected File createLocalFileFromDownloadsCopy(String path) throws Exception {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement elem = (WebElement) js.executeScript(
                    "var input = window.document.createElement('INPUT'); " +
                            "input.setAttribute('type', 'file'); " +
                            "input.style.display = 'none'; " +
                            "window.document.body.appendChild(input); " +
                            "return input;");

            assert elem != null;
            elem.sendKeys(path);

            Object result = js.executeAsyncScript(
                    "var input = arguments[0], callback = arguments[1]; " +
                            "var reader = new FileReader(); " +
                            "reader.onload = function (ev) { callback(reader.result); }; " +
                            "reader.onerror = function (ex) { callback(ex.message); }; " +
                            "reader.readAsDataURL(input.files[0]); "
                            + "input.remove();",
                    elem);

            if (result == null || !result.toString().startsWith("data:")) {
                throw new RuntimeException("Failed to get file content: " + result);
            }

            String base64String = result.toString().substring(result.toString().indexOf("base64,") + 7);

            String fileName = getFileNameFromPath(path);
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);

            String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1);

            File downloadedFile = File.createTempFile(fileName.replace("." + fileFormat, ""), "." + fileFormat);
            Files.write(Paths.get(downloadedFile.getAbsolutePath()), decodedBytes);
            logger.info("Local path: " + downloadedFile.getAbsolutePath());
            return downloadedFile;

        } catch (Exception e) {
            logger.warn("Error creating local file from downloads copy: " + e.getMessage());
            throw e;
        }
    }

    protected String getFileNameFromPath(String path) {
        if (path == null)
            return null;
        int lastUnixPos = path.lastIndexOf('/');
        int lastWindowsPos = path.lastIndexOf('\\');
        int index = Math.max(lastUnixPos, lastWindowsPos);
        return index > -1 ? path.substring(index + 1) : path;
    }
}
