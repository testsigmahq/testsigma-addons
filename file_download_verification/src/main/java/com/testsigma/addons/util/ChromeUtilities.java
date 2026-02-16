package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class ChromeUtilities implements DownloadUtilities {

    private WebDriver driver;
    private Logger logger;

    public ChromeUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public boolean searchFileNameInDownloads(String desiredFileName) {
        if (!driver.getCurrentUrl().startsWith("chrome://downloads")) {
            driver.get("chrome://downloads/");
        }

        try {
            String script = "return (function(targetFileName) {" +
                    "    const fileNames = [];" +
                    "    try {" +
                    "        const manager = document.querySelector('downloads-manager');" +
                    "        const root = manager?.shadowRoot;" +
                    "        if (!root) return false;" +
                    "        const lazyList = root.querySelector('cr-lazy-list');" +
                    "        const itemEls = lazyList ? Array.from(lazyList.children).filter(function(el) { return el.tagName === 'DOWNLOADS-ITEM'; }) : [];" +
                    "        for (var i = 0; i < itemEls.length; i++) {" +
                    "            var item = itemEls[i];" +
                    "            var sr = item.shadowRoot;" +
                    "            if (!sr) continue;" +
                    "            var content = sr.querySelector('#content');" +
                    "            if (content && content.classList.contains('show-progress')) continue;" +
                    "            var fileLink = sr.querySelector('#file-link');" +
                    "            var name = (fileLink && fileLink.getAttribute('title')) || (sr.querySelector('#name') && sr.querySelector('#name').textContent.trim());" +
                    "            if (name) fileNames.push(name);" +
                    "        }" +
                    "        var target = targetFileName.toLowerCase().trim();" +
                    "        return fileNames.some(function(n) { return n.toLowerCase() === target; });" +
                    "    } catch (error) {" +
                    "        console.error('Error getting completed file names:', error);" +
                    "        return false;" +
                    "    }" +
                    "})(" + escapeJsString(desiredFileName) + ");";

            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = js.executeScript(script);

            if (Boolean.TRUE.equals(result)) {
                logger.info("File found in downloads: " + desiredFileName);
                return true;
            }
            logger.info("There is no file with name: " + desiredFileName);
            return false;
        } catch (Exception e) {
            logger.warn("Error occurred: " + e.getMessage());
            throw new RuntimeException("Error occurred while searching for file in downloads", e);
        }
    }

    private String escapeJsString(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                default: sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }
}