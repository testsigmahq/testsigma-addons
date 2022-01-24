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
@Action(actionText = "Enter text on cell where value is test-data in the table element",
        description = "Enters the given value into the cell name that matches the input",
        applicationType = ApplicationType.WEB)
public class EnterOnCellNameWhereCellValueIsTestDataOfTable extends WebAction {

    private static final String SUCCESS_MESSAGE = "Expected Cell Was Clicked";
    private static final String TABLE_ERROR_MESSAGE = "Expected Table Was Not Found";
    private static final String CELL_ERROR_MESSAGE = "Expected Cell Was Not Found";
    private static final String ELEMENT_DISABLED = "Expected Cell Was Not Clicked. Element with locator <b>\"%s:%s\"</b> is in disabled state";
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData text;
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
                    List<WebElement> elements = cell.findElements(By.tagName("input"));
                    if (elements.size()>0 && testData.getValue().toString().equals(elements.get(0).getAttribute("name"))) {
                        cell.findElement(By.tagName("input")).sendKeys(text.getValue().toString());
                        setSuccessMessage(SUCCESS_MESSAGE);
                        return Result.SUCCESS;
                    }
                }
            }
            setErrorMessage(CELL_ERROR_MESSAGE);
            return Result.FAILED;
        } catch (Exception exception) {
            exception.printStackTrace();
            setErrorMessage(TABLE_ERROR_MESSAGE);
            return Result.FAILED;
        }

    }
}

