package com.testsigma.addons.broken_link_finder.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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
@Action(actionText = "Find all broken images in url and all child pages",
        description = "Returns all broken image links in the given application URL and all its child pages",
        applicationType = ApplicationType.WEB)
public class FindAllBrokenImagesInPageAndAllChildPages extends WebAction {

    public static HttpURLConnection huc = null;
    public static int respCode = 200;
    public static int anchorTagsWithEmptyURLs = 0;
    public static List<String> validatedLinks = new ArrayList<>();
    public static List<String> skippedURLs = new ArrayList<>();
    public static List<String> brokenURLs = new ArrayList<>();
    public static List<String> validatedImages = new ArrayList<>();
    public static List<String> brokenImages = new ArrayList<>();
    public static List<String> validLinks = new ArrayList<>();
    @TestData(reference = "url")
    private com.testsigma.sdk.TestData URL;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try {
            collectValidLinks(URL.getValue().toString(), 5);
            collectBrokenImages(URL.getValue().toString());
            validLinks.forEach(link -> collectBrokenImages(link));
            if (brokenImages.size() > 0) {
                setSuccessMessage("Broken Images [" + brokenImages.size() + "] : " + brokenImages);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("There are no Broken Images in the page");
                return Result.SUCCESS;
            }
        } catch (Exception exception) {
            log("Exception while finding broken images "+ exception);
            setErrorMessage("error while finding Broken Images ");
            return Result.FAILED;
        }

    }

    void collectBrokenImages(String URL) {
        driver.get(URL);
        driver.manage().window().maximize();
        try {
            List<WebElement> image_list = driver.findElements(By.tagName("img"));
            for (WebElement img : image_list) {
                if (img != null) {
                    String src = img.getAttribute("src");
                    if (validatedImages.contains(src)) {
                        if (brokenImages.contains(src)) {
                            log(img.getAttribute("outerHTML") + " has broken image.");
                        }
                    } else {
                        HttpClient client = HttpClientBuilder.create().build();
                        HttpGet request = new HttpGet(src);
                        HttpResponse response = client.execute(request);
                        if (response.getStatusLine().getStatusCode() != 200) {
                            log(img.getAttribute("outerHTML") + " has broken image.");
                            brokenImages.add(src);
                        } else{
                            validatedImages.add(src);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    Boolean collectValidLinks(String URL, Integer nestedIterationsLevel) {
        if(URL==null) return false;
        driver.get(URL);
        String href = "";
        String url = URL.substring(URL.indexOf("://")+3);
        url = url.indexOf("/") != -1 ? url.substring(0, url.indexOf("/")) : url;
        List<WebElement> links = driver.findElements(By.tagName("a"));
        Iterator<WebElement> it = links.iterator();


        while (it.hasNext()) {
            href = it.next().getAttribute("href");

            if (href == null || href.isEmpty() || href.startsWith("tel:")) {
                anchorTagsWithEmptyURLs++;
                log("URL is either not configured for anchor tag or it is empty");
                continue;
            }

            if (validatedLinks.contains(href)) {
                continue;
            }
            validatedLinks.add(href);
            if (!href.contains(url)) {
                skippedURLs.add(href);
                log("URL belongs to another domain, skipping it.");
                continue;
            }

            try {
                huc = (HttpURLConnection) (new URL(href).openConnection());

                huc.setRequestMethod("HEAD");

                huc.connect();

                respCode = huc.getResponseCode();

                if (respCode >= 400) {
                    brokenURLs.add(href);
                    log(href + " is a broken link");
                } else {
                    if (!href.equals(this.URL.getValue().toString())) {
                        validLinks.add(href);
                    }
                    log(href + " is a valid link");
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
                    log("Skipping Element as its URL is Null, Element - " + links.get(i));
                }
                return collected;
            }
        }
        return true;
    }
}