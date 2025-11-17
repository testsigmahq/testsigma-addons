package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(
        actionText = "Verify if the element element-locator has an empty text",
        description = "Verifies if the element has an empty text",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class VerifyIfElementhasEmptyValue extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element elementLocator;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;

        try {
            String text = elementLocator.getElement().getText();
            logger.info("text: " + text);

            if (text == null || text.trim().isEmpty()) {
                text = elementLocator.getElement().getAttribute("value");
                logger.info("Input value is empty");
            }

            if (!text.trim().isEmpty()) {
                result = Result.FAILED;
                logger.info("Element text is NOT empty");
                setErrorMessage("Element locator doesn't have an empty text. Actual text: '" + text + "'");
                return result;
            }

        } catch (Exception e) {
            result = Result.FAILED;
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
            return result;
        }

        setSuccessMessage("Element has empty text");
        return result;
    }
}
