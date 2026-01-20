package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Set chrome permission to URL: Target-URL , Permission Name: Permission-Name, Permission Type: Permission-Value", description = "Set chrome site settings permissions to URL.", applicationType = ApplicationType.MOBILE_WEB, useCustomScreenshot = false)
public class MyFirstWebAction extends WebAction {

    @TestData(reference = "Target-URL")
    private com.testsigma.sdk.TestData targetUrl;

    @TestData(reference = "Permission-Name")
    private com.testsigma.sdk.TestData permissionName;

    @TestData(reference = "Permission-Value")
    private com.testsigma.sdk.TestData permissionValue;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        String origWindowHandle = driver.getWindowHandle();
        List<String> origWindowsList = new ArrayList<>(driver.getWindowHandles());

        logger.info("Initiating execution");

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Open Chrome site settings in a new tab
            logger.info("Opening chrome settings in new tab");
            String chromeSettingsUrl = "chrome://settings/content/siteDetails?site=" + targetUrl.getValue().toString();
            driver.switchTo().newWindow(WindowType.TAB);
            String newTab = driver.getWindowHandles()
                    .stream()
                    .filter(handle -> !origWindowsList.contains(handle))
                    .findFirst()
                    .get();
            driver.switchTo().window(newTab);
            driver.get(chromeSettingsUrl);
            Thread.sleep(5000);

            // Determine which permission selector to use
            Boolean isOldStructure = (Boolean) js.executeScript(
                    "return !!document.querySelector('body > settings-ui')?.shadowRoot.querySelector('#main')?.shadowRoot.querySelector('settings-basic-page');");

            logger.info("Is old structure flag: " + isOldStructure);
            Boolean isChrome143Structure = (Boolean) js.executeScript(
                    "return !!document.querySelector('body > settings-ui')?.shadowRoot.querySelector('#main')?.shadowRoot.querySelector('settings-privacy-page-index')?.shadowRoot.querySelector('#siteSettingsSiteDetails');");

            logger.info("Is Chrome 143+ structure: " + isChrome143Structure);
            String permissionSelect;

            if (isChrome143Structure) {
                logger.info("Its Chrome 143+ Structure");
                permissionSelect = "return document.querySelector(\"body > settings-ui\")\n" +
                        "  .shadowRoot.querySelector(\"#main\")\n" +
                        "  .shadowRoot.querySelector(\"settings-privacy-page-index\")\n" +
                        "  .shadowRoot.querySelector(\"#siteSettingsSiteDetails\")\n" +
                        "  .shadowRoot.querySelector(\"site-details-permission[label='%s']\")\n" +
                        "  .shadowRoot.querySelector(\"#permission\")";
            } else if (isOldStructure) {
                logger.info("Its Old Structure");
                permissionSelect = "return document.querySelector(\"body > settings-ui\").shadowRoot.querySelector(\"#main\").shadowRoot"
                        +
                        ".querySelector(\"settings-basic-page\").shadowRoot.querySelector(\"#basicPage > settings-section.expanded > settings-privacy-page\").shadowRoot"
                        +
                        ".querySelector(\"#pages > settings-subpage > site-details\").shadowRoot" +
                        ".querySelector(\"div.list-frame > site-details-permission[label='%s']\").shadowRoot.querySelector(\"#permission\")";
            } else {
                logger.info("Its New Structure (pre-143)");
                permissionSelect = "return document.querySelector(\"body > settings-ui\")\n" +
                        "  .shadowRoot.querySelector(\"#main\")\n" +
                        "  .shadowRoot.querySelector(\"#privacy > settings-privacy-page-index\")\n" +
                        "  .shadowRoot.querySelector(\"#old\")\n" +
                        "  .shadowRoot.querySelector(\"#basicPage > settings-section > settings-privacy-page\")\n" +
                        "  .shadowRoot.querySelector(\"#pages > settings-subpage > site-details\")\n" +
                        "  .shadowRoot.querySelector(\"div.list-frame > site-details-permission[label='%s']\")\n" +
                        "  .shadowRoot.querySelector(\"#permission\")";
            }

            // Format the query with the permission name
            permissionSelect = String.format(permissionSelect, permissionName.getValue().toString());
            logger.info("Permission query: " + permissionSelect);

            // Find the element and set the permission
            logger.info("Finding permission type element: " + permissionName.getValue().toString());
            WebElement elem = (WebElement) js.executeScript(permissionSelect);
            Select select = new Select(elem);
            logger.info("Selecting permission type: " + permissionValue.getValue().toString());
            select.selectByVisibleText(permissionValue.getValue().toString());
            Thread.sleep(3000);

            logger.info("Permission successfully set");

        } catch (Exception error) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Exception: " + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Unable to change site permissions: " + ExceptionUtils.getMessage(error));
            return result;
        } finally {
            driver.switchTo().window(origWindowHandle);
        }

        setSuccessMessage("Site permissions changed successfully");
        return result;
    }
}
