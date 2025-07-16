package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Data
@Action(
        actionText = "Mouse Hover on element element-locator",
        description = "Hover on element",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false
)
public class MouseOvertheElement extends WebAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @Override
  public Result execute() {
    logger.info("Initiating double tap and hold action");
    Result result = Result.SUCCESS;

    try {
      AppiumDriver driver1 = (AppiumDriver) this.driver;
      WebElement targetElement = element.getElement();

      Actions actions = new Actions(driver1);
      actions.moveToElement(targetElement).build().perform();

      setSuccessMessage("Successfully performed hover on the element");
    } catch (Exception e) {
      result = Result.FAILED;
      logger.warn("Exception during double tap and hold: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to perform double tap and hold. Error: " + ExceptionUtils.getMessage(e));
    }

    return result;
  }
}