package com.testsigma.addons.click_until.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

import java.util.List;

@Data
@Action(actionText = "Click on element until text test-data is visible",
        description = "Performs Click action as long an element is visible",
        applicationType = ApplicationType.WEB)
public class ClickUntilTextIsVisible extends WebAction {

    private static final int NO_OF_CLICKS = 20;
    private static final Integer dummy = 1;
    private static final String SUCCESS_MESSAGE = "Expected text is found. Total no of clicks performed: <b>%s</b>";
    private static final String ERROR_MESSAGE = "Expected text is not found. Total no of clicks performed: <b>%s</b>";
    private static final String SUCCESS_MESSAGE_TEXT_PRESENT_BEFORE_CLICK = "Expected text is already present in current page, Not performing any click action.";
    private static final String ELEMENT_DISABLED = "Element with locator <b>\"%s:%s\"</b> is in disabled state. No of clicks made: <b>%s</b>";
    private static WebElement displayedElement;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;

    private List<WebElement> elements;
    private int noOfClicksPerformed = 0;

    @Override
    public Result execute() throws NoSuchElementException {
        String Xpath = "//*[contains(text(),'" + this.testData.getValue() + "')]";
        boolean isTextFound = isTextPresent(Xpath);
        if (isTextFound) {
            setSuccessMessage(SUCCESS_MESSAGE_TEXT_PRESENT_BEFORE_CLICK);
            return Result.SUCCESS;
        }

        for (int i = 0; i < NO_OF_CLICKS; i++) {
            try {
                findElement();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Assert.isTrue(element.getElement().isEnabled(), String.format(ELEMENT_DISABLED, element.getBy(), element.getValue(), i));
            element.getElement().click();
            noOfClicksPerformed++;
            isTextFound = isTextPresent(Xpath);
            if (isTextFound) {
                setSuccessMessage(String.format(SUCCESS_MESSAGE, i + 1));
                return Result.SUCCESS;
            }
        }
        //We need to fail the case if it comes here.
        setErrorMessage(String.format(ERROR_MESSAGE, NO_OF_CLICKS));
        return Result.FAILED;
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

    protected void findElement(String Xpath) throws Exception {
        //CustomExpectedConditions.explictWait(getDriver(), elementSearchCriteria.getBy(), getTimeout().intValue());
        elements = this.driver.findElements(By.xpath(Xpath));
        if (!elements.isEmpty()) {
            setDisplayedElement();
        } else {
            throw new NoSuchElementException(String.format("Element could not be found using the given criteria - <b>\"%s:%s\"</b>", element.getBy(), element.getValue()));
        }
    }

    private void setDisplayedElement() {
        this.displayedElement = getElements().get(0);
        for (WebElement webElement : elements) {
            if (webElement.isEnabled()) {
                this.displayedElement = webElement;
                break;
            }
        }
    }

    public boolean elementContainsText(List<WebElement> elements, String testData) {
        for (WebElement element : elements) {
            String text = element.getText();
            if (text != null && text.contains(testData)) {
                return true;
            }
        }
        return false;
    }

    protected void findElement() throws Exception {
        elements = this.driver.findElements(this.element.getBy());
        if (!elements.isEmpty()) {
            setDisplayedElement();
        } else {
            throw new NoSuchElementException(String.format("Element could not be found using the given criteria - <b>\"%s:%s\"</b>", element.getBy(), element.getValue()));
        }
    }
}

