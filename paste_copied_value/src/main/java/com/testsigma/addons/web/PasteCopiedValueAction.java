package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Data
@Action(actionText = "Paste the copied value in the element-locator",
        description = "To paste the copied value in the element",
        applicationType = ApplicationType.WEB)

public class PasteCopiedValueAction extends WebAction {

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        Actions action = new Actions(driver);
        try {
            WebElement element = this.element.getElement();
            element.click();
            action.keyDown(Keys.COMMAND);
            action.sendKeys("v");
            action.keyUp(Keys.COMMAND);
            action.build().perform();
        } catch (AssertionError error) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info(ExceptionUtils.getStackTrace(error));
            setErrorMessage("Copied Value is not pasted properly");
        }
        setSuccessMessage("Copied value is pasted successfully on the mentioned locator");
        return result;
    }

}
