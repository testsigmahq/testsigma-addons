package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;

@Data
@Action(actionText = "Open the latest downloaded file from the downloads and store the path in runtime variable test-data",
        description = "Opens the latest downloaded file and stores its path. Supports Chrome and Edge",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class OpenLatestDownloadedFile extends WebAction {

  @TestData(reference = "test-data", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData testdata;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;


  @Override
  protected Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName().toLowerCase();
    String filePath = null;
    try {

      // Wait until all downloads are complete, with a timeout
      if (!waitUntilDownloadsComplete(browserName, 30)){
        logger.warn("Downloads did not complete within the timeout period");
        setErrorMessage("Downloads did not complete within the timeout period");
        return Result.FAILED;
      }

      if (browserName.contains("chrome")) {
        filePath = handleChromeDownloads();
      } else if (browserName.contains("edge")) {
        filePath = handleEdgeDownloads();
      } else {
        logger.warn("Unsupported browser: " + browserName);
        setErrorMessage("Unsupported browser: " + browserName);
        return Result.FAILED;
      }
      if(filePath != null){
        runTimeData = new com.testsigma.sdk.RunTimeData();
        runTimeData.setValue(filePath);
        runTimeData.setKey(testdata.getValue().toString());
      }else {
        return Result.FAILED;
      }
    } catch (Exception e) {
      logger.warn("An error occurred: " + e.getMessage() + e);
      setErrorMessage("An error occurred: " + e.getMessage());
      return Result.FAILED;

    }
    return result;
  }

  private String handleChromeDownloads() {
    driver.navigate().to("chrome://downloads/");
    JavascriptExecutor js = (JavascriptExecutor) driver;
    // Get the latest downloaded file path
    String filePath = (String) js.executeScript(
            "return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList').items[0].filePath;"
    );
    logger.info("Latest downloaded file path: " + filePath);

    // Click on the latest downloaded file
    WebElement latestFile = (WebElement) js.executeScript(
            "return document.querySelector('body > downloads-manager').shadowRoot.querySelector('#frb0').shadowRoot.querySelector('#file-link');"
    );
    if(latestFile != null){
      latestFile.click();
      logger.info("Clicked on the latest downloaded file.");
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else{
      logger.warn("No file found in the downloads page.");
      setErrorMessage("No file found in the downloads page.");
      return null;
    }


    return filePath;
  }

  private String handleEdgeDownloads() {
    driver.navigate().to("edge://downloads/all");

    JavascriptExecutor js = (JavascriptExecutor) driver;

    // Updated JavaScript to find and return the first file path
    String script = "return (function() {" +
            "    let filePaths = [];" +
            "    let downloadItems = document.querySelectorAll('div[role=\"listitem\"]');" +
            "    downloadItems.forEach(item => {" +
            "        let fileNameElement = item.querySelector('button[aria-label]');" +
            "        if (fileNameElement) {" +
            "            let fileName = fileNameElement.getAttribute('aria-label');" +
            "            if (fileName) {" +
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
            "    return filePaths.length > 0 ? filePaths[0] : 'No file found';" +
            "})();";

    // Execute the JavaScript to get the file path
    String filePath = (String) js.executeScript(script);

    // Process the result
    if (filePath == null || "No file found".equals(filePath)) {
      logger.warn("No file found in the downloads page.");
      setErrorMessage("No file found in the downloads page.");
      return null;
    }
    logger.info("Latest downloaded file path: " + filePath);

    // Find the button element and click it
    try {
      // The following selector assumes the file button is the aria-label button itself
      WebElement downloadButton = (WebElement) js.executeScript(
              "let downloadItems = document.querySelectorAll('div[role=\"listitem\"]');" +
                      " for(item of downloadItems){" +
                      "    let fileNameElement = item.querySelector('button[aria-label]');" +
                      "    if(fileNameElement){" +
                      "        let fileName = fileNameElement.getAttribute('aria-label');" +
                      "        if(fileName != null && fileName.length > 0){"+
                      "            let img = item.querySelector('img');" +
                      "            if (img) {" +
                      "               let src = img.getAttribute('src');" +
                      "                if(src){"+
                      "                    try{" +
                      "                       let url = decodeURIComponent(src.split('path=')[1].split('&')[0]).replace(/\\+/g, ' ');"+
                      "                       if(url == arguments[0]){ return fileNameElement; }"+
                      "                   } catch(e){console.error('Error decoding src', e);}" +
                      "                }"+
                      "            }"+
                      "         }"+
                      "    }"+
                      " } return null;", filePath
      );
      if(downloadButton != null){
        downloadButton.click();
        logger.info("Clicked on the latest downloaded file.");
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }else{
        logger.warn("Download button not found");
        setErrorMessage("Download button not found");
        return null;
      }


    } catch (Exception e) {
      logger.warn("Error clicking on the download button: " + e);
      setErrorMessage("Error clicking on the download button: " + e.getMessage());
      return null;
    }

    return filePath;
  }
  private boolean isFileDownloaded(String browserName) {

    if (browserName.contains("edge")) {
      return isFileDownloadedEdge();
    } else if (browserName.contains("chrome")) {
      return isFileDownloadedChrome();
    } else {
      logger.warn("Unsupported browser for download check: " + browserName);
      return true; // Consider downloads as complete for unsupported browsers
    }
  }


  private boolean isFileDownloadedEdge() {
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

  private boolean isFileDownloadedChrome() {
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


  private boolean waitUntilDownloadsComplete(String browserName, int timeoutInSeconds) {
    long startTime = System.currentTimeMillis();
    long timeoutMillis = timeoutInSeconds * 1000L;

    while (System.currentTimeMillis() - startTime < timeoutMillis) {
      if (isFileDownloaded(browserName)) {
        logger.info("All downloads are complete");
        return true;
      }
      try {
        Thread.sleep(1000); // Wait 1 second before checking again
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warn("Waiting for download was interrupted");
        return false;
      }

    }
    logger.warn("Download timeout reached");
    return false; // Timeout reached, downloads did not complete
  }
}