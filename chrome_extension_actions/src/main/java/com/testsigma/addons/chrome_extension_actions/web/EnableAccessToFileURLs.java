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
@Action(actionText = "enable Allow access to file URLs for the chrome extension name",
        description = "If an extension needs to access file:// URLs, users will need to enable access for those features inside the extension's details page.\n" +
                "This action allows to enable or disable the option to access file exensions",
        applicationType = ApplicationType.WEB)
public class EnableAccessToFileURLs extends WebAction {

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
        return enableAccessToFileUrls();
    }

    Result enableAccessToFileUrls() {
            try {
                String viewInChromeWebStore = "//extensions-manager -->shadow-root --> #viewManager > extensions-detail-view -->shadow-root --> #allow-on-file-urls -->shadow-root --> #crToggle";
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
                Boolean enabled = Boolean.valueOf((String) js.executeScript("return arguments[0].getAttribute('aria-pressed')", parentElem));
                Boolean operation = enable.getValue().toString().equals("Enable")? true : false;
                if (operation.equals(enabled)) {
                    setErrorMessage("Allow access to file URLs is already " + (enable.getValue().toString().equals("true") ? "enabled" : "disabled"));
                    return Result.FAILED;
                } else {
                    js.executeScript("return arguments[0].click()", parentElem);
                    setSuccessMessage("Successfully " + (enable.getValue().toString().equals("true") ? "enabled" : "disabled") + " access to file URLs");
                    return Result.SUCCESS;
                }
            } catch (Exception exception) {
                setErrorMessage("Could not Click on the View in Chrome Web Store");
                return Result.FAILED;
            }

    }
}

