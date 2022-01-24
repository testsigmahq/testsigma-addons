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
@Action(actionText = "Enter text on cell with test-data on row number in the table element",
        description = "Enters the given value into the cell with a the provided row number and table",
        applicationType = ApplicationType.WEB)
public class EnterOnCellWithTextInRowNumberOfTable extends WebAction {

    private static final String SUCCESS_MESSAGE = "Expected Cell Was Clicked";
    private static final String TABLE_ERROR_MESSAGE = "Expected Table Was Not Found";
    private static final String ROW_ERROR_MESSAGE = "Expected Row Was Not Found";
    private static final String CELL_ERROR_MESSAGE = "Expected Cell Was Not Found";
    private static final String ELEMENT_DISABLED = "Expected Cell Was Not Clicked. Element with locator <b>\"%s:%s\"</b> is in disabled state";
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "text")
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
            if (allrows.size() >= Integer.parseInt(number.getValue().toString())) {
                WebElement row = allrows.get(Integer.parseInt(number.getValue().toString()));
                List<WebElement> cells = row.findElements(By.tagName("td"));
                for (WebElement cell : cells) {
                    List<WebElement> elements = cell.findElements(By.tagName("input"));
                    if (elements.size()>0 && testData.getValue().toString().equals(elements.get(0).getAttribute("name"))) {
                        cell.findElement(By.tagName("input")).sendKeys(text.getValue().toString());
                        setSuccessMessage(SUCCESS_MESSAGE);
                        return Result.SUCCESS;
                    }
                }
                setErrorMessage(CELL_ERROR_MESSAGE);
                return Result.FAILED;
            } else {
                setErrorMessage(ROW_ERROR_MESSAGE);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            setErrorMessage(TABLE_ERROR_MESSAGE);
            return Result.FAILED;
        }

    }
}

