package com.testsigma.addons.chrome_extension_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Click on Details button for the chrome extension name",
        description = "This action returns the details of the installed extension for a given extension name",
        applicationType = ApplicationType.WEB)
public class ClickOnDetailsButton extends WebAction {

    @TestData(reference = "name")
    private com.testsigma.sdk.TestData name;

    @Override
    public Result execute() throws NoSuchElementException {
        try{
            driver.get("chrome://extensions/");
            String extensionsQuery = "//extensions-manager -->shadow-root --> #viewManager > #items-list -->shadow-root --> extensions-item";
            String[] locators = extensionsQuery.split("-->");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement parentElem = null;
            List<WebElement> parentElems = new ArrayList<>();
            WebElement shadowDom = null;
            for (String locator : locators) {
                if (locator.trim().equalsIgnoreCase("shadow-root")) {
                    shadowDom = (WebElement) js.executeScript("return arguments[0].shadowRoot", parentElem);
                } else {
                    if (shadowDom != null) {
                        if (locator.equals(" extensions-item")) {
                            parentElems = shadowDom.findElements(By.cssSelector(locator.trim()));
                        } else {
                            parentElem = shadowDom.findElement(By.cssSelector(locator.trim()));
                        }
                    } else {
                        if (locator.equals(" extensions-item")) {
                            parentElems = driver.findElements(By.xpath(locator.trim()));
                        } else {
                            parentElem = driver.findElement(By.xpath(locator.trim()));
                        }
                    }
                }
            }
            if (parentElems.size() > 0) {
                int i;
                for (i = 0; i < parentElems.size(); i++) {
                    WebElement parentElemsShadowRoot = (WebElement) js.executeScript("return arguments[0].shadowRoot", parentElems.get(i));
                    try {
                        WebElement pluginCard = parentElemsShadowRoot.findElement(By.id("name"));
                        if (pluginCard.getText().equals(name.getValue().toString())) {
                            try {
                                parentElemsShadowRoot.findElement(By.id("detailsButton")).click();
                                setSuccessMessage("Successfully Clicked on the Details Button");
                                return Result.SUCCESS;
                            } catch (Exception exception) {
                                setErrorMessage(String.format("Extension with the name %s not found", name.getValue().toString()));
                                return Result.FAILED;
                            }
                        }
                    } catch (Exception exception) {
                        System.out.println("ignore");
                    }
                    ;
                }
                setErrorMessage(String.format("Extension with the name %s not found", name.getValue().toString()));
                return Result.FAILED;
            } else {
                setErrorMessage(String.format("Extension with the name %s not found", name.getValue().toString()));
                return Result.FAILED;
            }
        }catch(Exception exception){
            exception.printStackTrace();
            setErrorMessage(exception.getMessage());
            return Result.FAILED;
        }

    }
}