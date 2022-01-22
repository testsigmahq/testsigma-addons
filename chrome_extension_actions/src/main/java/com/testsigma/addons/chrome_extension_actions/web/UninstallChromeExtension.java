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
@Action(actionText = "Uninstall chrome extension name",
        description = "Removes an extension that has been installed for a given extension name",
        applicationType = ApplicationType.WEB)
public class UninstallChromeExtension extends WebAction {


    @TestData(reference = "name")
    private com.testsigma.sdk.TestData name;

    @Override
    public Result execute() throws NoSuchElementException {
        ClickOnDetailsButton clickOnDetailsButton = new ClickOnDetailsButton();
        clickOnDetailsButton.setName(name);
        clickOnDetailsButton.setDriver(driver);
        clickOnDetailsButton.execute();
        return this.uninstallExtension();
    }

    private Result uninstallExtension() {
        try{
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String viewInChromeWebStore = "//extensions-manager -->shadow-root --> #viewManager > extensions-detail-view -->shadow-root --> #remove-extension";
            String[] locators = viewInChromeWebStore.split("-->");
            WebElement parentElem = null;
            WebElement shadowDom = null;
            for (String locator : locators) {
                if (locator.trim().equalsIgnoreCase("shadow-root")) {
                    shadowDom = (WebElement) js.executeScript("return arguments[0].shadowRoot", parentElem);
                } else {
                    if (shadowDom != null) {
                        try {
                            parentElem = shadowDom.findElement(By.cssSelector(locator.trim()));
                            System.out.println(parentElem);
                        } catch (Exception exception) {
                            System.out.println("errr");
                        }
                    } else {
                        parentElem = driver.findElement(By.xpath(locator.trim()));
                    }
                }
            }
            js.executeScript("document.querySelector(\"extensions-manager\").shadowRoot.querySelector(\"cr-view-manager > extensions-detail-view\").shadowRoot.querySelector(\"cr-link-row:nth-of-type(5)\").shadowRoot.querySelector(\"div[id='labelWrapper']\").querySelector(\"div\").click()");
            driver.switchTo().alert().accept();
            setSuccessMessage("Successfully uninstalled Chrome");
            return Result.SUCCESS;
        }catch(Exception exception){
           exception.printStackTrace();
            setErrorMessage("Failed uninstalled Chrome");
            return Result.FAILED;
        }

    }
}

