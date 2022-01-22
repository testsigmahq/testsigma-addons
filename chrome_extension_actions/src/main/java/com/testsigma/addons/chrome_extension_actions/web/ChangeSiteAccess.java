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
@Action(actionText = "Change the site access to access-level for the chrome extension name",
        description = "This action will control to certain given sites that the extension can access",
        applicationType = ApplicationType.WEB)
public class ChangeSiteAccess extends WebAction {

    @TestData(reference = "name")
    private com.testsigma.sdk.TestData name;

    @TestData(reference = "access-level", allowedValues = {"On all sites", "On click"})
    private com.testsigma.sdk.TestData accessLevel;

    @Override
    public Result execute() throws NoSuchElementException {
        ClickOnDetailsButton clickOnDetailsButton = new ClickOnDetailsButton();
        clickOnDetailsButton.setName(name);
        clickOnDetailsButton.setDriver(driver);
        clickOnDetailsButton.execute();
        return changeSiteAccess();
    }

    Result changeSiteAccess() {
        try {
            String changeSiteAccess = "//extensions-manager -->shadow-root --> #viewManager > extensions-detail-view -->shadow-root --> extensions-runtime-host-permissions -->shadow-root -->  #host-access";
            String[] locators = changeSiteAccess.split("-->");
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
            js.executeScript("return arguments[0].value='" + accessLevel.getValue().toString() + "'", parentElem);
            setSuccessMessage("Successfully Clicked on the View in Chrome Web Store");
            return Result.SUCCESS;
        } catch (Exception exception) {
            setErrorMessage("Could not Click on the View in Chrome Web Store");
            return Result.FAILED;
        }
    }
}

