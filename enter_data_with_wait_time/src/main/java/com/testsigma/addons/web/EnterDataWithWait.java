package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Enter value into element-locator with a time gap of time-in-ms milliseconds between characters",
        description = "This action types the given data into a UI element," +
                " character by character, with a specified delay between each keystroke.",
        applicationType = ApplicationType.WEB)
public class EnterDataWithWait extends WebAction {

    @TestData(reference = "value")
    private com.testsigma.sdk.TestData testData;

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "time-in-ms")
    private com.testsigma.sdk.TestData timeGap;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution of Action");
        Result result = Result.SUCCESS;

        try {
            WebElement webElement = element.getElement();
            String dataToType = testData.getValue().toString();

            long delay;
            try {
                delay = Long.parseLong(timeGap.getValue().toString());
            } catch (NumberFormatException e) {
                setErrorMessage("Invalid time gap provided. Please enter a valid number for milliseconds.");
                return Result.FAILED;
            }

            for (char character : dataToType.toCharArray()) {
                webElement.sendKeys(String.valueOf(character));
                logger.info("Typed character: " + character);
                Thread.sleep(delay);
            }

            setSuccessMessage(String.format("Successfully typed '%s' into the element '%s' with a delay of %dms between characters.",
                    dataToType, element.getValue(), delay));

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage(String.format("An unexpected error occurred while trying to type into element '%s': %s",
                    element.getValue(), e.getMessage()));
            logger.warn("An unexpected error occurred: " + ExceptionUtils.getStackTrace(e));
        }

        return result;
    }
}