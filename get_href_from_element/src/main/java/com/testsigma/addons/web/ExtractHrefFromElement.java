package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(
        actionText = "Extract href attribute from element element_locator and store in runtime variable variable-name",
        description = "Extracts the href attribute from a specified element and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB
)
public class ExtractHrefFromElement extends WebAction {

  @Element(reference = "element_locator")
  private com.testsigma.sdk.Element elementLocator;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    logger.info("Starting execution of ExtractHref action.");
    Result result = Result.SUCCESS;

    try {
      logger.info("Locating element using: " + elementLocator.getBy());
      WebElement element = driver.findElement(elementLocator.getBy());

      String hrefValue = element.getAttribute("href");
      logger.info("Extracted href attribute value: " + hrefValue);

      if (hrefValue != null && !hrefValue.isEmpty()) {
        runTimeData.setKey(variableName.getValue().toString());
        runTimeData.setValue(hrefValue);
        setSuccessMessage("Successfully extracted href: '" + hrefValue + "' and stored in runtime variable '" + variableName.getValue() + "'");
        logger.info("Successfully stored href '" + hrefValue + "' in runtime variable '" + variableName.getValue() + "'.");
      } else {
        setErrorMessage("Href attribute not found in the specified element.");
        logger.warn("Href attribute not found in the element with locator: " + elementLocator.getBy());
        result = Result.FAILED;
      }
    } catch (NoSuchElementException e) {
      setErrorMessage("Element with locator '" + elementLocator.getBy() + "' not found: " + e.getMessage());
      logger.warn("Element with locator '" + elementLocator.getBy() + "' not found." + e);
      result = Result.FAILED;
    } catch (Exception e) {
      setErrorMessage("An unexpected error occurred: " + e.getMessage());
      logger.warn("Unexpected error during execution" + e);
      result = Result.FAILED;
    }
    return result;
  }
}
