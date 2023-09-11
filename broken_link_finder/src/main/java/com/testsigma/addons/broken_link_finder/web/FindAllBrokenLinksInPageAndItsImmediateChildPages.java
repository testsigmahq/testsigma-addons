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
@Action(actionText = "Find all broken links in url and immediate child pages",
        description = "This action returns all broken links in the given application URL and its immediate child pages",
        applicationType = ApplicationType.WEB)
public class FindAllBrokenLinksInPageAndItsImmediateChildPages extends WebAction {

    public static HttpURLConnection huc = null;
    public static int respCode = 200;
    public static List<String> validatedLinks = new ArrayList<>();
    public static int anchorTagsWithEmptyURLs = 0;
    public static List<String> skippedURLs = new ArrayList<>();
    public static List<String> brokenURLs = new ArrayList<>();
    @TestData(reference = "url")
    private com.testsigma.sdk.TestData URL;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try{
            collectBrokenUrls(URL.getValue().toString());
            validatedLinks.forEach(link -> collectBrokenUrls(link));
            System.out.println(validatedLinks);
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

    void collectBrokenUrls(String URL) {
        driver.get(URL);

        String url = URL.substring(URL.indexOf("://")+3);
        url = url.indexOf("/") != -1 ? url.substring(0, url.indexOf("/")) : url;
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

            if (!href.startsWith(url)) {
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
                    System.out.println(href + " is a valid link");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}