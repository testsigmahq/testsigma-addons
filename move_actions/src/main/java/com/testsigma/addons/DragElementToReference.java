package com.testsigma.addons;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;


@Action(actionText = "Drag the element element-locator to the reference element reference-element",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class DragElementToReference extends WebAction {
    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;
    @Element(reference = "reference-element")
    private com.testsigma.sdk.Element referenceElement;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("your awesome code starts here");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            WebElement webElement = element.getElement();
            WebElement webElement1 = referenceElement.getElement();
            Actions actions = new Actions(driver);
            actions.moveToElement(webElement)
                    .clickAndHold()
                    .moveToElement(referenceElement.getElement())
                    .release().build().perform();

            setSuccessMessage("Successfully drag the element to reference element reference-element");
        } catch (NoSuchElementException e){
            setErrorMessage("Element not found : " + e.getMessage());
            logger.info("Element not found : " + ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
