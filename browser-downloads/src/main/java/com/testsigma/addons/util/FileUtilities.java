package com.testsigma.addons.util;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class FileUtilities {

    WebDriver driver;
    Logger logger;
    public FileUtilities(WebDriver driver, Logger logger){
        this.driver = driver;
        this.logger = logger;
    }
    public File copyFileFromDownloads(String fileFormat,String fileName) throws Exception{
        String currentWindowHandle = driver.getWindowHandle();
        File downloadedFile = null;
try {
    ((JavascriptExecutor) driver).executeScript("window.open()");
    Set<String> allWindows = driver.getWindowHandles();
    ArrayList<String> tabs = new ArrayList<>(allWindows);
    driver.switchTo().window(tabs.get(tabs.size() - 1));

    driver.navigate().to("chrome://downloads/");
    WebDriverWait ww = new WebDriverWait(driver, Duration.ofMillis(60000));
    ww.until(new ExpectedCondition<Boolean>() {
        @Override
        public Boolean apply(WebDriver driver) {
            return isFileDownloaded();
        }
    });
    String remoteFilePath = null;
    if (fileName != null) {
        remoteFilePath = getFilePathByFileNameInDownloads(fileName);
    } else {
        remoteFilePath = getDownloadedFileLocalPath();
    }
    logger.info("Downloaded file path=" + remoteFilePath);
     downloadedFile = createLocalFileFromDownloadsCopy(remoteFilePath, fileFormat);
}catch(Exception e){
    throw e;
}finally{
    driver.switchTo().window(currentWindowHandle);
}
        //switch to parent window tab

        return downloadedFile;
    }
    private boolean isFileDownloaded() {

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

    private String getDownloadedFileLocalPath() {


        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object obj = js.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items[0].filePath; ");
        if(obj == null){
            throw new RuntimeException("No files in the downloads");
        }
        return obj.toString();
    }
    private String getFilePathByFileNameInDownloads(String desiredFileName) {
        String script = "return Array.from(document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items)" +
                "    .find(item => item.fileName && item.fileName.includes('" + desiredFileName + "')).filePath;";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object obj = js.executeScript(script);
        if(obj == null){
            throw new RuntimeException("There is no file with name:"+desiredFileName);
        }
        return obj.toString();
    }
    private File createLocalFileFromDownloadsCopy(String path, String fileFormat) throws Exception {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement elem = (WebElement) js.executeScript("var input = window.document.createElement('INPUT'); " +
                "input.setAttribute('type', 'file'); " +
                "input.hidden = true; " +
                "input.onchange = function (e) { e.stopPropagation() }; " +
                "return window.document.documentElement.appendChild(input); ");

        //elem._execute('sendKeysToElement', {'value': [path ],'text':path})
        elem.sendKeys(path);
        long start = System.currentTimeMillis();
        Object result = js.executeAsyncScript("var input = arguments[0], callback = arguments[1]; " +
                        "var reader = new FileReader(); " +
                        "reader.onload = function (ev) { callback(reader.result) }; " +
                        "reader.onerror = function (ex) { callback(ex.message) }; " +
                        "reader.readAsDataURL(input.files[0]); " +
                        "input.remove(); "
                , elem);

        long end = System.currentTimeMillis(); System.out.println("Time taken: "+(end-start));
        if (result == null || !result.toString().startsWith("data:")) {
            throw new RuntimeException("Failed to get file content: " + result);
        }
        String base64String = result.toString().substring(result.toString().indexOf("base64")+7);
        File f = new File(path);
        String fileName = f.getName();
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        File downloadedFile = File.createTempFile("test","."+fileFormat);
        // String data = new String(decodedBytes);
        System.out.println("fileName: "+downloadedFile.getName());
        logger.info("Local path"+downloadedFile.getAbsolutePath());
        Files.write(Paths.get(downloadedFile.getAbsolutePath()), decodedBytes);
        return downloadedFile;

    }






}
