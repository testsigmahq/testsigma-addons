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
@Action(actionText = "Enter text on cell number where cell value is test-data in the table element",
        description = "Enters the given value into the cell number that matches the input",
        applicationType = ApplicationType.WEB)
public class EnterOnCellNumberWhereCellValueIsTestDataOfTable extends WebAction {

    private static final String SUCCESS_MESSAGE = "Expected Cell Was Clicked";
    private static final String TABLE_ERROR_MESSAGE = "Expected Table Was Not Found";
    private static final String CELL_ERROR_MESSAGE = "Expected Cell Was Not Found";
    private static final String ELEMENT_DISABLED = "Expected Cell Was Not Clicked. Element with locator <b>\"%s:%s\"</b> is in disabled state";
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData text;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "number")
    private com.testsigma.sdk.TestData number;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;

    @Override
    public Result execute() throws NoSuchElementException {
        WebElement table;
        try {
            table = driver.findElement(element.getBy());
            List<WebElement> allCells = table.findElements(By.tagName("td"));
            if (allCells.size() >= Integer.parseInt(number.getValue().toString())) {
                WebElement cell = allCells.get(Integer.parseInt(number.getValue().toString()));
                if (testData.getValue().toString().equals(cell.findElement(By.tagName("input")).getAttribute("value"))) {
                    cell.findElement(By.tagName("input")).sendKeys(text.getValue().toString());
                    setSuccessMessage(SUCCESS_MESSAGE);
                    return Result.SUCCESS;
                } else {
                    setErrorMessage(CELL_ERROR_MESSAGE);
                    return Result.FAILED;
                }
            } else {
                setErrorMessage(CELL_ERROR_MESSAGE);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            setErrorMessage(TABLE_ERROR_MESSAGE);
            return Result.FAILED;
        }

    }
}

