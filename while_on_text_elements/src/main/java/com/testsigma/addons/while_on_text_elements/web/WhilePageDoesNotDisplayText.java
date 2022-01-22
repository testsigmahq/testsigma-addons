package com.testsigma.addons.while_on_text_elements.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
@Action(actionText = "While the page does not display text",
        actionType = StepActionType.WHILE_LOOP,
        description = "Performs actions while current page does not display the provided text",
        applicationType = ApplicationType.WEB)
public class WhilePageDoesNotDisplayText extends WebAction {

    private static final String ERROR_MESSAGE = "Page displays the text";
    private static final String SUCCESS_MESSAGE = "Page does not display the text";
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData testData;
    private List<WebElement> elements;
    private WebElement element;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        String Xpath = "//*[text()='" + this.testData.getValue() + "']";
        boolean isTextFound = isTextPresent(Xpath);
        if (isTextFound) {
            setErrorMessage(ERROR_MESSAGE);
            return Result.FAILED;
        }

        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }

    private boolean isTextPresent(String dynamic_elementVarName) {
        try {
            findElement(dynamic_elementVarName);
            if (getElements() != null && !getElements().isEmpty()) {
                boolean containsText = elementContainsText(getElements(), this.testData.getValue().toString());
                if (containsText) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.info("Ignore this error," + e);
        }
        return false;
    }

    protected void findElement(String Xpath) {
        elements = this.driver.findElements(By.xpath(Xpath));
        if (!elements.isEmpty()) {
            setDisplayedElement();
        } else {
            throw new NoSuchElementException(String.format("Element could not be found using the given criteria - <b>\"%s:%s\"</b>", "Xpath", Xpath));

        }
    }

    private void setDisplayedElement() {
        this.element = getElements().get(0);
        for (WebElement webElement : elements) {
            if (webElement.isEnabled()) {
                this.element = webElement;
                break;
            }
        }
    }

    public boolean elementContainsText(List<WebElement> elements, String testData) {
        for (WebElement webElement : elements) {
            String displayValue = webElement.getCssValue("display");
            if (!displayValue.equals("none")) {
                try {
                    String visibility = webElement.getCssValue("visibility");
                    if (!(visibility.equals("collapse") || visibility.equals("hidden"))) {
                        this.element = webElement;
                        String text = element.getText();
                        if (text != null && text.equals(testData)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    logger.info("Ignore this error," + e);
                }
            }
        }
        return false;
    }
}

