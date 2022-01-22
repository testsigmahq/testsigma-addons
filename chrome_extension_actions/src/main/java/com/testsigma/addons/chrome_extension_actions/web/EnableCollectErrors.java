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

@Data
@Action(actionText = "enable Collect errors for chrome extension name",
        description = "The action to allow or block errors thrown by an extension",
        applicationType = ApplicationType.WEB)
public class EnableCollectErrors extends WebAction {

    @TestData(reference = "name")
    private com.testsigma.sdk.TestData name;

    @TestData(reference = "enable", allowedValues = {"Enable","Disable"})
    private com.testsigma.sdk.TestData enable;

    @Override
    public Result execute() throws NoSuchElementException {
        ClickOnDetailsButton clickOnDetailsButton = new ClickOnDetailsButton();
        clickOnDetailsButton.setName(name);
        clickOnDetailsButton.setDriver(driver);
        clickOnDetailsButton.execute();
        enableDeveloperMode();
        return enableCollectErrors();
    }

    private void enableDeveloperMode() {

       try{
           String developerModeButton = "//extensions-manager -->shadow-root --> extensions-toolbar -->shadow-root --> #devMode";
           String[] locators = developerModeButton.split("-->");
           JavascriptExecutor js = (JavascriptExecutor) driver;
           WebElement parentElem = null;
           WebElement shadowDom = null;
           for (String locator : locators) {
               if (locator.trim().equalsIgnoreCase("shadow-root")) {
                   shadowDom = (WebElement) js.executeScript("return arguments[0].shadowRoot", parentElem);
               } else {
                   if (shadowDom != null) {
                       parentElem = shadowDom.findElement(By.cssSelector(locator.trim()));
                   } else {
                       parentElem = driver.findElement(By.xpath(locator.trim()));
                   }
               }
           }
           Boolean enabled = Boolean.valueOf((String) js.executeScript("return arguments[0].getAttribute('aria-pressed')", parentElem));
           if (enabled.equals(false)) {
               js.executeScript("return arguments[0].click()", parentElem);
           }
       }catch(Exception e){

       }
    }

    Result enableCollectErrors() {
        String viewInChromeWebStore = "//extensions-manager -->shadow-root --> #viewManager > extensions-detail-view -->shadow-root --> #collect-errors -->shadow-root --> #crToggle";
        String[] locators = viewInChromeWebStore.split("-->");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement parentElem = null;
        WebElement shadowDom = null;
        for (String locator : locators) {
            if (locator.trim().equalsIgnoreCase("shadow-root")) {
                shadowDom = (WebElement) js.executeScript("return arguments[0].shadowRoot", parentElem);
            } else {
                if (shadowDom != null) {
                    parentElem = shadowDom.findElement(By.cssSelector(locator.trim()));
                } else {
                    parentElem = driver.findElement(By.xpath(locator.trim()));
                }
            }
        }
        try {
            Boolean enabled = Boolean.valueOf((String) js.executeScript("return arguments[0].getAttribute('aria-pressed')", parentElem));
            Boolean operation = enable.getValue().toString().equals("Enable")? true : false;
            if (operation.equals(enabled)) {
                setErrorMessage("Collect errors is already " + (enable.getValue().toString().equals("true") ? "enabled" : "disabled"));
                return Result.FAILED;
            } else {
                js.executeScript("return arguments[0].click()", parentElem);
                setSuccessMessage("Successfully " + (enable.getValue().toString().equals("true") ? "enabled" : "disabled") + " Collect errors");
                return Result.SUCCESS;
            }
        } catch (Exception exception) {
            setErrorMessage("Could not Click on the Collect errors toggle button");
            return Result.FAILED;
        }
    }
}