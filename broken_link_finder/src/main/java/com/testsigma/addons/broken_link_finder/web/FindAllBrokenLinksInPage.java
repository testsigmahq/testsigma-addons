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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
        try{
            String homePage = url.getValue().toString();
            String url = "";
            driver.get(homePage);
            List<WebElement> links = driver.findElements(By.tagName("a"));
            HttpURLConnection huc = null;
            int respCode = 200;
            List<String> validatedLinks = new ArrayList<>();
            int anchorTagsWithEmptyURLs = 0;
            List<String> skippedURLs = new ArrayList<>();
            List<String> brokenURLs = new ArrayList<>();

            Iterator<WebElement> it = links.iterator();

            while (it.hasNext()) {
                url = it.next().getAttribute("href");
                if (url == null || url.isEmpty()) {
                    anchorTagsWithEmptyURLs++;
                    System.out.println("URL is either not configured for anchor tag or it is empty");
                    continue;
                }

                if (validatedLinks.contains(url)) {
                    continue;
                }

                validatedLinks.add(url);
                System.out.println(url);

                if (!url.startsWith(homePage)) {
                    skippedURLs.add(url);
                    System.out.println("URL belongs to another domain, skipping it.");
                    continue;
                }

                try {
                    huc = (HttpURLConnection) (new URL(url).openConnection());

                    huc.setRequestMethod("HEAD");

                    huc.connect();

                    respCode = huc.getResponseCode();

                    if (respCode >= 400) {
                        brokenURLs.add(url);
                        System.out.println(url + " is a broken link");
                    } else {
                        System.out.println(url + " is a valid link");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (brokenURLs.size() > 0) {
                setErrorMessage(" brokenURLs : " + brokenURLs);
                return Result.FAILED;
            } else {
                setSuccessMessage("There are no Broken links in the page");
                return Result.SUCCESS;
            }
        }catch (Exception exception){
            setErrorMessage("error while finding Broken Images ");
            return Result.FAILED;
        }
    }
}