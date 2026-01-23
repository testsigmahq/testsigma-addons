package com.testsigma.addons.web.util;

import com.testsigma.sdk.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public abstract class BaseUtilities implements Utilities {

    protected WebDriver driver;
    protected Logger logger;

    // Define chunk size: 1 MB is safe for almost all WebDriver implementations
    private static final long CHUNK_SIZE = 1024 * 1024;

    public BaseUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
        try {
            ZipSecureFile.setMinInflateRatio(0);
        } catch (Exception e) {
            logger.warn("Could not set ZipSecureFile min inflate ratio. Ignore if not using Excel files: " + e.getMessage());
        }
    }

    protected File createLocalFileFromDownloadsCopy(String path) throws Exception {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            /* Create or reuse file input element in the DOM */
            js.executeScript(
                    "var input = document.getElementById('tempFileInput');" +
                            "if (!input) {" +
                            "  input = document.createElement('input');" +
                            "  input.type = 'file';" +
                            "  input.id = 'tempFileInput';" +
                            "  document.body.appendChild(input);" +
                            "}" +
                            "input.style.display = 'block';" +
                            "input.style.position = 'fixed';" +
                            "input.style.top = '20px';" +
                            "input.style.left = '20px';" +
                            "input.style.zIndex = '9999';"
            );

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement fileInput = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("tempFileInput"))
            );

            /* Upload the file to the browser's file input */
            fileInput.sendKeys(path);

            /* Get File Details (Size, Name, Type) */
            Object fileDetails = js.executeScript(
                    "var f = arguments[0].files[0];" +
                            "if (!f) return null;" +
                            "return {" +
                            "  name: f.name," +
                            "  size: f.size," +
                            "  type: f.type" +
                            "};",
                    fileInput
            );

            if (fileDetails == null) {
                throw new RuntimeException("No file selected after sendKeys()");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) fileDetails;

            long sizeInBytes = ((Number) details.get("size")).longValue();

            // Log file details
            double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
            logger.info("=========== FILE DETAILS ===========");
            logger.info("File Name       : " + details.get("name"));
            logger.info("File Size (MB)  : " + String.format("%.2f", sizeInMB));
            logger.info("====================================");

            if (sizeInBytes == 0) {
                throw new RuntimeException("Uploaded file size is 0 bytes");
            }

            /* Prepare Local File to store the incoming data */
            String originalFileName = getFileNameFromPath(path);
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            File tempFile = File.createTempFile("ts_", extension);

            /* Read file in CHUNKS to prevent WebDriver Crash/Timeout */
            // We use BufferedOutputStream to write chunks directly to disk
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                long offset = 0;

                // JavaScript to slice the file and read a specific chunk
                String chunkScript =
                        "var input = arguments[0];" +
                                "var offset = arguments[1];" +
                                "var chunkSize = arguments[2];" +
                                "var callback = arguments[arguments.length - 1];" + // Async callback

                                "var file = input.files[0];" +
                                "if (!file) { callback(null); return; }" +

                                "var blob = file.slice(offset, offset + chunkSize);" +
                                "var reader = new FileReader();" +

                                "reader.onload = function(e) {" +
                                "  var data = e.target.result;" +
                                "  var base64 = data.split(',')[1];" +
                                "  callback(base64);" +
                                "};" +

                                "reader.onerror = function(e) { callback(null); };" +
                                "reader.readAsDataURL(blob);";

                while (offset < sizeInBytes) {
                    // Call the script
                    Object result = js.executeAsyncScript(chunkScript, fileInput, offset, CHUNK_SIZE);

                    if (result == null) {
                        throw new RuntimeException("Failed to read chunk from browser at offset: " + offset);
                    }

                    // Decode the Base64 chunk
                    byte[] chunkBytes = Base64.getDecoder().decode(result.toString());

                    // Write immediately to disk (keeps RAM usage low)
                    bos.write(chunkBytes);

                    offset += CHUNK_SIZE;

                    // Log progress every ~5MB to avoid spamming logs
                    if (offset % (CHUNK_SIZE * 5) == 0) {
                        logger.debug("Transferred " + (offset / 1024 / 1024) + "MB...");
                    }
                }
                // Ensure all data is written to disk
                bos.flush();
            }

            logger.info("Local file successfully created at: " + tempFile.getAbsolutePath());
            return tempFile;

        } catch (Exception e) {
            logger.warn("Error creating local file from downloads copy: " + e.getMessage());
            throw e;
        }
    }

    protected String getFileNameFromPath(String path) {
        if (path == null) return null;
        int lastUnixPos = path.lastIndexOf('/');
        int lastWindowsPos = path.lastIndexOf('\\');
        int index = Math.max(lastUnixPos, lastWindowsPos);
        return index > -1 ? path.substring(index + 1) : path;
    }
}