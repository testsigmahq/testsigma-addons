package com.testsigma.addons.broken_link_finder.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
@Action(actionText = "Check & report all console errors in url and all child pages",
        description = "Collects any possible error message given by the browser in a given URL all its child pages",
        applicationType = ApplicationType.WEB)
public class ReportErrorsInPageAndAllChildPages extends WebAction {

    public static HttpURLConnection huc = null;
    public static int respCode = 200;
    public static int anchorTagsWithEmptyURLs = 0;
    public static List<String> validatedLinks = new ArrayList<>();
    public static List<String> skippedURLs = new ArrayList<>();
    public static List<String> brokenURLs = new ArrayList<>();
    public static List<String> validLinks = new ArrayList<>();
    @TestData(reference = "url")
    private com.testsigma.sdk.TestData URL;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            driver.get(URL.getValue().toString());
            LogEntries logEntries = driver.manage().logs().get("browser");
            List<LogEntry> logEntryList = logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE)).collect(Collectors.toList());
            this.collectValidLinks(URL.getValue().toString(), 5);
            validLinks.forEach(links -> {
                driver.navigate().to(URL.getValue().toString());
                logEntryList.addAll(logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE)).collect(Collectors.toList()));
            });
            if (!logEntryList.isEmpty()) {
                setSuccessMessage(" Errors [" + logEntryList.size() + "] : " + Arrays.toString(logEntryList.toArray()));
                return Result.SUCCESS;
            } else {
                setSuccessMessage("There are no console errors in the page");
                return Result.SUCCESS;
            }
        } catch (Exception exception) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(exception));
            setErrorMessage("error while finding Broken Images ");
            return Result.FAILED;
        }

    }

    Boolean collectValidLinks(String url, Integer nestedIterationsLevel) throws Exception {
        if (url == null || url.isEmpty() || url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("javascript:"))
            return false;
        driver.get(url);
        String url1 = url.substring(url.indexOf("://") + 3);
        url1 = url1.contains("/") ? url1.substring(0, url1.indexOf("/")) : url1;
        String href = "";
        List<WebElement> links = driver.findElements(By.tagName("a"));

        for (WebElement link : links) {
            try {
                href = link.getAttribute("href");
            } catch (StaleElementReferenceException exception) {
                link = waitForElementToBeNonStale(driver, link);
                if (link == null) {
                    logger.info("Skipping element as it is a stale element");
                    continue;
                }
                href = link.getAttribute("href");
                if (href == null) {
                    logger.info("Skipping Element as its 'href' is " + href + ", Element - " + link);
                    continue;
                }
            }

            if (href == null || href.isEmpty() || href.startsWith("tel:") || href.startsWith("mailto:") || href.startsWith("javascript:")) {
                anchorTagsWithEmptyURLs++;
                System.out.println("URL is either not configured for anchor tag or it is empty");
                continue;
            }

            if (validatedLinks.contains(href)) {
                continue;
            }
            validatedLinks.add(href);
            System.out.println(href);

            if (!href.startsWith(url1)) {
                skippedURLs.add(href);
                System.out.println("URL belongs to another domain, skipping it.");
                continue;
            }

            try {
                huc = (HttpURLConnection) (new URL(href).openConnection());

                huc.setRequestMethod("HEAD");

                huc.connect();

                respCode = huc.getResponseCode();

                if (respCode >= 400) {
                    brokenURLs.add(href);
                    System.out.println(href + " is a broken link");
                } else {
                    if (!href.equals(this.URL.getValue().toString())) {
                        validLinks.add(href);
                    }
                    System.out.println(href + " is a valid link");
                }

            } catch (IOException e) {
                throw new Exception(e);
            }
        }

        if (nestedIterationsLevel > 0) {
            nestedIterationsLevel--;
            Integer nextNestedIterationsLevel = nestedIterationsLevel;
            for (int i = 0; i < links.size(); i++) {
                WebElement element = links.get(i);
                String link = null;
                try {
                    link = element.getAttribute("href");

                } catch (Exception exception) {
                    element = waitForElementToBeNonStale(driver, element);
                    if (element == null) {
                        logger.info("Skipping element as it is a stale element");
                        continue;
                    }
                    link = element.getAttribute("href");
                }
                if (link == null) {
                    logger.info("Skipping Element as its 'href' is " + element.getAttribute("href") + ", Element - " + element);
                    continue;
                }
                Boolean collected = collectValidLinks(link, nextNestedIterationsLevel);
                if (!collected) {
                    logger.info("Skipping Element as its 'href' is " + element.getAttribute("href") + ", Element - " + element);
                }
            }

        }
        return true;
    }

    static WebElement waitForElementToBeNonStale(WebDriver driver, WebElement webElement) throws Exception {
        try {
            Thread.sleep(4000);
            webElement.getText();
            return webElement;
        } catch (StaleElementReferenceException e) {
            return null;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }


}