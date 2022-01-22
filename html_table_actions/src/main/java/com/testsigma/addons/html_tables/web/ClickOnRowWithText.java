package com.testsigma.addons.html_tables.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
@Action(actionText = "Click in the row with text test-data of table element",
        description = "Performs click action on the row that contains a specific text",
        applicationType = ApplicationType.WEB)
public class ClickOnRowWithText extends WebAction {

    private static final String SUCCESS_MESSAGE = "Expected Row Was Clicked";
    private static final String TABLE_ERROR_MESSAGE = "Expected Table Was Not Found";
    private static final String ROW_ERROR_MESSAGE = "Expected Row Was Not Found";
    private static final String ELEMENT_DISABLED = "Expected Row Was Not Clicked. Element with locator <b>\"%s:%s\"</b> is in disabled state";
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData text;
    @TestData(reference = "number")
    private com.testsigma.sdk.TestData number;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;

    @Override
    public Result execute() throws NoSuchElementException {
        WebElement table;
        try {
            table = driver.findElement(element.getBy());
            List<WebElement> allrows = table.findElements(By.tagName("tr"));
            for (WebElement row : allrows) {
                List<WebElement> Cells = row.findElements(By.tagName("td"));
                for (WebElement cell : Cells) {
                    if (cell.getText().equals(text.getValue().toString())) {
                        if (!row.isEnabled()) {
                            setErrorMessage(String.format(ELEMENT_DISABLED, element.getBy(), element.getValue()));
                            return Result.FAILED;
                        }
                        row.click();
                        setSuccessMessage(SUCCESS_MESSAGE);
                        return Result.SUCCESS;
                    }
                }
            }
            setErrorMessage(ROW_ERROR_MESSAGE);
            return Result.FAILED;
        } catch (Exception exception) {
            exception.printStackTrace();
            setErrorMessage(TABLE_ERROR_MESSAGE);
            return Result.FAILED;
        }

    }
}

