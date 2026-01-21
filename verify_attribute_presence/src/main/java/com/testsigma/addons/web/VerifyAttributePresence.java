package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.NoSuchElementException;

@Data
@Action(
        actionText = "Verify if attribute attribute-name is attribute-state on element element-locator",
        description = "Verifies whether a specific attribute is present or not present",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class VerifyAttributePresence extends WebAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;

  @TestData(reference = "attribute-name")
  private com.testsigma.sdk.TestData attributeName;

  @TestData(reference = "attribute-state", allowedValues = {"PRESENT", "NOT PRESENT"})
  private com.testsigma.sdk.TestData attributeState;

  @Override
  public Result execute() throws NoSuchElementException {

    try {
      boolean isPresent;

      try {
        String value = element.getElement().getAttribute(attributeName.getValue().toString());
        logger.info("Value is " + value);
        isPresent = (value != null);
      } catch (Exception e) {
        logger.warn("Exception Occurred " + e.getMessage());
        isPresent = false;
      }

      switch (attributeState.getValue().toString().toUpperCase()) {

        case "PRESENT":
          if (isPresent) {
            logger.info("Attribute '" + attributeName.getValue() + "' is PRESENT on the element.");
            setSuccessMessage(
                    "Attribute '" + attributeName.getValue() + "' is PRESENT on the element."
            );
            return Result.SUCCESS;
          } else {
            logger.warn("Attribute '" + attributeName.getValue() + "' is NOT present on the element.");
            setErrorMessage(
                    "Attribute '" + attributeName.getValue() + "' is NOT present on the element."
            );
            return Result.FAILED;
          }

        case "NOT PRESENT":
          if (!isPresent) {
            logger.info("Attribute '" + attributeName.getValue() + "' is NOT present on the element.");
            setSuccessMessage(
                    "Attribute '" + attributeName.getValue() + "' is NOT present on the element."
            );
            return Result.SUCCESS;
          } else {
            logger.warn("Attribute '" + attributeName.getValue() + "' is PRESENT on the element.");
            setErrorMessage(
                    "Attribute '" + attributeName.getValue() + "' is PRESENT on the element."
            );
            return Result.FAILED;
          }

        default:
          setErrorMessage(
                  "Invalid attribute-state provided. Use PRESENT or NOT PRESENT."
          );
          return Result.FAILED;
      }

    } catch (Exception e) {
      logger.warn("Error while verifying attribute presence: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage(
              "Failed to verify attribute presence due to exception: " + ExceptionUtils.getMessage(e)
      );
      return Result.FAILED;
    }
  }
}
