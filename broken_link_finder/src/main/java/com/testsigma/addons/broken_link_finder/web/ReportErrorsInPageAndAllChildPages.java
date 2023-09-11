package com.testsigma.addons.broken_link_finder.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try{
            driver.get(URL.getValue().toString());
            LogEntries logEntries = driver.manage().logs().get("browser");
            List<LogEntry> logEntryList = new ArrayList<>();
            logEntryList.addAll(logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE)).collect(Collectors.toList()));
            this.collectValidLinks(URL.getValue().toString(), 5);
            validLinks.forEach(links -> {
                driver.navigate().to(URL.getValue().toString());
                logEntryList.addAll(logEntries.getAll().stream().filter(logEntry -> logEntry.getLevel().equals(Level.SEVERE)).collect(Collectors.toList()));
            });
            if (logEntryList.size() > 0) {
                setSuccessMessage(" Errors [" + logEntryList.size() + "] : " + logEntryList.toArray());
                return Result.SUCCESS;
            } else {
                setSuccessMessage("There are no console errors in the page");
                return Result.SUCCESS;
            }
        }catch (Exception exception){
            setErrorMessage("error while finding Broken Images ");
            return Result.FAILED;
        }

    }

    Boolean collectValidLinks(String url, Integer nestedIterationsLevel) {
        if(url == null || url.isEmpty() || url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("javascript:") ) return false;
        driver.get(url);
        String url1 = url.substring(url.indexOf("://") + 3);
        url1 = url1.indexOf("/") != -1 ? url1.substring(0, url1.indexOf("/")) : url1;
        String href = "";
        List<WebElement> links = driver.findElements(By.tagName("a"));

        Iterator<WebElement> it = links.iterator();

        while (it.hasNext()) {
            href = it.next().getAttribute("href");

            if (href == null || href.isEmpty() || href.startsWith("tel:") || href.startsWith("mailto:") || href.startsWith("javascript:") ) {
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

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (nestedIterationsLevel > 0) {
            nestedIterationsLevel--;
            Integer nextNestedIterationsLevel = nestedIterationsLevel;
            for(int i= 0; i< links.size();i++){
                Boolean collected = collectValidLinks(links.get(i).getAttribute("href"), nextNestedIterationsLevel);
                if(collected ==  false){
                    logger.info("Skipping Element as its 'href' is "+ links.get(i).getAttribute("href") +", Element - " + links.get(i));
                }
            }

        }
        return true;
    }

}