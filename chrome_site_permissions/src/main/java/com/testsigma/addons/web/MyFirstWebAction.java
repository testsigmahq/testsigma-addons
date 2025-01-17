package com.testsigma.addons.web;

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
@Action(actionText = "Set chrome permission to URL: Target-URL , Permission Name: Permission-Name, Permission Type: Permission-Value",
        description = "Set chrome site settings permissions to URL.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class MyFirstWebAction extends WebAction {

  @TestData(reference = "Target-URL")
  private com.testsigma.sdk.TestData targetUrl;

  @TestData(reference = "Permission-Name")
  private com.testsigma.sdk.TestData permissionName;

  @TestData(reference = "Permission-Value")
  private com.testsigma.sdk.TestData permissionValue;

  public static void main(String... a){
  /*  MyFirstWebAction action = new MyFirstWebAction();
    action.setTargetUrl(new com.testsigma.sdk.TestData("https://www.facebook.com"));
    action.setPermissionName(new com.testsigma.sdk.TestData("Microphone"));
    action.setPermissionValue(new com.testsigma.sdk.TestData("Allow"));
    WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    RemoteWebDriver driver = new ChromeDriver(options);
    action.setDriver(driver);
    action.execute();
*/
  }

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String origWindowHandle = driver.getWindowHandle();
    List<String> origwindowsList = new ArrayList<>(driver.getWindowHandles());

    logger.info("Initiating execution");
    String permissionSelect = "return document.querySelector(\"body > settings-ui\").shadowRoot.querySelector(\"#main\").shadowRoot" +
            ".querySelector(\"settings-basic-page\").shadowRoot.querySelector(\"#basicPage > settings-section.expanded > settings-privacy-page\").shadowRoot" +
            ".querySelector(\"#pages > settings-subpage > site-details\").shadowRoot" +
            ".querySelector(\"div.list-frame > site-details-permission[label='%s']\").shadowRoot.querySelector(\"#permission\")";
    permissionSelect = String.format(permissionSelect, permissionName.getValue().toString());
    logger.info("Permission query:"+permissionSelect);

    try {

      JavascriptExecutor js = (JavascriptExecutor) driver;
      logger.info("Opening chrome settings in new tab");
      String chromeSettingsUrl = "chrome://settings/content/siteDetails?site="+targetUrl.getValue().toString();
      driver.switchTo().newWindow(WindowType.TAB);
      String newTab = driver.getWindowHandles()
              .stream()
              .filter(handle -> !origwindowsList.contains(handle))
              .findFirst()
              .get();
      driver.switchTo().window(newTab);

      logger.info("Opening new tab");

        driver.get(chromeSettingsUrl);
        Thread.sleep(5000);
        logger.info("Finding permission type element:"+permissionName.getValue().toString());
        WebElement elem = (WebElement) js.executeScript(permissionSelect);
        Select select = new Select(elem);
        logger.info("Selecting permission type:"+permissionValue.getValue().toString());
        select.selectByVisibleText(permissionValue.getValue().toString());
        Thread.sleep(3000);
        logger.info("Permisssion successfully set");
    } catch (Exception error) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.info("Exception: "+ ExceptionUtils.getStackTrace(error));
      setErrorMessage("Unable to change site permissions,"+error.getMessage());
      return result;
    }finally {
      driver.switchTo().window(origWindowHandle);
    }
    setSuccessMessage("Site permissions changed successfully");
    return result;
  }
}