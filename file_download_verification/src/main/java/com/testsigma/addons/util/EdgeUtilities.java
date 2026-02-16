package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.List;

public class EdgeUtilities implements DownloadUtilities {

    private WebDriver driver;
    private Logger logger;

    public EdgeUtilities(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    @Override
    public boolean searchFileNameInDownloads(String desiredFileName) {
        if (!driver.getCurrentUrl().startsWith("edge://downloads")) {
            driver.get("edge://downloads");
        }

        try {
            String script = "return (function(targetFileName) {" +
                    "    const fileNames = [];" +
                    "    try {" +
                    "        const app = document.querySelector('body > downloads-app')?.shadowRoot;" +
                    "        if (!app) return false;" +
                    "        const cards = app.querySelectorAll('edge-card');" +
                    "        for (const card of cards) {" +
                    "            const isRemoved = card.classList.contains('doesNotExist') || card.querySelector('.downloads_removedContainer');" +
                    "            if (isRemoved) continue;" +
                    "            const titleEl = card.querySelector('.downloads_itemTitle');" +
                    "            if (titleEl) {" +
                    "                const name = (titleEl.getAttribute('aria-label') || titleEl.getAttribute('title') || titleEl.textContent || '').trim();" +
                    "                if (name) fileNames.push(name);" +
                    "            }" +
                    "        }" +
                    "        const target = targetFileName.toLowerCase().trim();" +
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