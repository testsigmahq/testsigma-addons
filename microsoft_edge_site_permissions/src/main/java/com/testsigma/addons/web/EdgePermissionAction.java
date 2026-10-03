package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Set edge permission to URL: Target-URL , Permission Name: Permission-Name, Permission Type: Permission-Value",
        description = "Set edge site settings permissions to URL.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class EdgePermissionAction extends WebAction {

  @TestData(reference = "Target-URL")
  private com.testsigma.sdk.TestData targetUrl;

  @TestData(reference = "Permission-Name")
  private com.testsigma.sdk.TestData permissionName;

  @TestData(reference = "Permission-Value")
  private com.testsigma.sdk.TestData permissionValue;

  private static final String SETTINGS_URL_PREFIX = "edge://settings/content/siteDetails?site=";

  @Override
  public com.testsigma.sdk.Result execute() {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String permissionName_ = permissionName.getValue().toString();
    String permissionValue_ = permissionValue.getValue().toString();
    String origWindowHandle = driver.getWindowHandle();
    List<String> origWindowsList = new ArrayList<>(driver.getWindowHandles());
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    try {
      String edgeSettingsUrl = SETTINGS_URL_PREFIX + targetUrl.getValue().toString();

      logger.info("Opening New Tab");
      // Open settings page in new tab, switch to it
      driver.switchTo().newWindow(WindowType.TAB);
      String newTab = driver.getWindowHandles()
              .stream()
              .filter(handle -> !origWindowsList.contains(handle))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Could not find new tab window handle"));

      // Wait for the new tab to be available
      wait.until(ExpectedConditions.numberOfWindowsToBe(origWindowsList.size() + 1));
      Thread.sleep(1500); // Added sleep after new tab

      logger.info("Switching To New Tab");
      driver.switchTo().window(newTab);
      logger.info("Opening Edge settings in a new tab: " + edgeSettingsUrl);
      driver.get(edgeSettingsUrl);

      // Wait for the page to load and be ready
      wait.until(ExpectedConditions.urlContains(edgeSettingsUrl));
      Thread.sleep(1500); // Added sleep after navigating to URL

      // Wait for the permission element to be present and clickable
      logger.info("Waiting for permission element: " + permissionName_);
      WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
              "//div[text()='" + permissionName_ + "']/following::button[contains(@id,'selecttrigger')]"
      )));
      Thread.sleep(1000);

      logger.info("Setting permission: " + permissionName_ + " to " + permissionValue_);
      setPermission(driver, permissionName_, permissionValue_, wait, dropdownButton);
      Thread.sleep(1500);


      logger.info("Refreshing Page");
      driver.navigate().refresh();
      Thread.sleep(1500);  // Added sleep after refresh
      // Wait for page to fully load after refresh, and an element to be present
      wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

    } catch (NoSuchElementException e) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Failed to locate element or settings are not loaded." + e);
      setErrorMessage("Unable to locate settings or element, check if the target URL is valid: " + e.getMessage());
      return result;
    } catch (TimeoutException e) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Timeout while waiting for elements to load." + e);
      setErrorMessage("Timeout waiting for elements to be loaded: " + e.getMessage());
      return result;
    } catch (Exception e) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("An unexpected error occurred while setting permissions." + e);
      setErrorMessage("An unexpected error occurred during permission changes: " + e.getMessage());
      return result;
    } finally {
      // Switch back to original window, ignore if the original window is already closed
      try {
        logger.info("Switching Back to Original Tab");
        driver.switchTo().window(origWindowHandle);
        Thread.sleep(1500); //Added a sleep after switching to the original window
        // wait for the original window to load before proceeding
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
      } catch (NoSuchWindowException e) {
        logger.warn("Original window not found, may be closed during execution: " + e.getMessage());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    setSuccessMessage("Site permissions changed successfully");
    return result;
  }

  public void setPermission(WebDriver driver, String permissionName, String permissionValue, WebDriverWait wait, WebElement dropdownButton) {
    logger.info("Clicking on dropdown button");
    dropdownButton.click();

    // Locate the options by role option and containing text
    logger.info("Selecting the option:" + permissionValue);
    WebElement targetOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@role='option' and .//span[text()='" + permissionValue + "']]")
    ));
    targetOption.click();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}