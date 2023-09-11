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

import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Find all broken images in url",
        description = "This action returns a list of all broken images in the given application URL",
        applicationType = ApplicationType.WEB)
public class FindAllBrokenImagesInPage extends WebAction {

    @TestData(reference = "url")
    private com.testsigma.sdk.TestData URL;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        try {
            driver.get(URL.getValue().toString());
            driver.manage().window().maximize();
            List<String> brokenImages = new ArrayList<>();
            List<WebElement> image_list = new ArrayList<>();
            try {
                image_list = driver.findElements(By.tagName("img"));
                for (WebElement img : image_list) {
                    if (img != null) {
                        HttpClient client = HttpClientBuilder.create().build();
                        String src = img.getAttribute("src");
                        try {
                            if (src != null) {
                                HttpGet request = new HttpGet(src);
                                HttpResponse response = client.execute(request);
                                if (response.getStatusLine().getStatusCode() != 200) {
                                    System.out.println(img.getAttribute("outerHTML") + " has broken image.");
                                    brokenImages.add(img.getAttribute("src"));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log(e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log(e.getMessage());
            }

            log("Total images in the page : " + image_list.size() + ", broken images : " + brokenImages.size() + " .");
            System.out.println("Total images in the page : " + image_list.size() + ", broken images : " + brokenImages.size() + " .");

            if (brokenImages.size() > 0) {
                setSuccessMessage(" brokenImages : " + brokenImages);
                return Result.SUCCESS;
            } else {
                setSuccessMessage("There are no Broken Images in the page");
                return Result.SUCCESS;
            }
        }catch(Exception exception){
            exception.printStackTrace();
            setErrorMessage("unable to find brokenImages");
            return Result.FAILED;
        }

    }
}