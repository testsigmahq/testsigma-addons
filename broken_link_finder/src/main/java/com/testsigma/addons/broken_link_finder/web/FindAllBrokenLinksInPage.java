package com.testsigma.addons.broken_link_finder.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Find all broken links in url",
        description = "This action checks for broken links on a given application URL",
        applicationType = ApplicationType.WEB)
public class FindAllBrokenLinksInPage extends WebAction {

    @TestData(reference = "url")
    private com.testsigma.sdk.TestData url;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            String homePage = url.getValue().toString();
            String currentUrl = "";
            String domainUrl = homePage.substring(homePage.indexOf("://") + 3);
            domainUrl = domainUrl.indexOf("/") != -1 ? domainUrl.substring(0, domainUrl.indexOf("/")) : domainUrl;
            driver.get(homePage);

            HttpURLConnection huc = null;
            int respCode = 200;
            List<String> validatedLinks = new ArrayList<>();
            int anchorTagsWithEmptyURLs = 0;
            List<String> skippedURLs = new ArrayList<>();
            List<String> brokenURLs = new ArrayList<>();

            // Fetch links inside the loop to avoid stale element

            List<WebElement> links;


            List<WebElement> anchorTags = driver.findElements(By.tagName("a"));

            for (int i = 0; i < anchorTags.size(); i++) {
                try {

                    links = driver.findElements(By.tagName("a"));
                    WebElement link = links.get(i);


                    currentUrl = link.getAttribute("href");

                    if (currentUrl == null || currentUrl.isEmpty()) {
                        anchorTagsWithEmptyURLs++;
                        logger.warn("URL is either not configured for anchor tag or it is empty");
                        continue;
                    }

                    if (validatedLinks.contains(currentUrl)) {
                        continue;
                    }

                    validatedLinks.add(currentUrl);

                    if (!currentUrl.contains(domainUrl)) {
                        skippedURLs.add(currentUrl);
                        logger.info("URL: " + currentUrl + " belongs to another domain, skipping it.");
                        continue;
                    }

                    try {
                        huc = (HttpURLConnection) (new URL(currentUrl).openConnection());

                        huc.setRequestMethod("HEAD");

                        huc.connect();

                        respCode = huc.getResponseCode();

                        if (respCode >= 400) {
                            brokenURLs.add(currentUrl);
                            logger.warn("URL: " + currentUrl + " is a broken link. Response Code: " + respCode);
                        } else {
                            logger.info("URL: " + currentUrl + " is a valid link. Response Code: " + respCode);
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        logger.warn("Malformed URL Exception for URL: " + currentUrl + e);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.warn("IO Exception for URL: " + currentUrl + e);
                    }
                }
                catch(StaleElementReferenceException e){
                    e.printStackTrace();
                    logger.warn("StaleElementReferenceException caught for URL, refreshing and retrying"+ e);
                    i--;
                }


            }


            if (brokenURLs.size() > 0) {
                setSuccessMessage(" Broken URLs : " + brokenURLs);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("There are no Broken links in the page");
                return Result.SUCCESS;
            }
        } catch (Exception exception) {
            logger.warn("Error while finding Broken Images " + exception);
            setErrorMessage("Error while finding Broken Images ");
            return Result.FAILED;
        }
    }
}