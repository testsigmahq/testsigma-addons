package com.testsigma.addons.html_tables.web;

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

import java.util.List;

@Data
@Action(actionText = "Store total number of cells in the table element into a variable variable-name",
        description = "Stores the total no of cells value present in the given table into the provided variable",
        applicationType = ApplicationType.WEB)
public class StoreTotalNumberOfCellsOfTable extends WebAction {

    private static final String SUCCESS_MESSAGE = "Stored total number of rows into : <b>%s</b>";
    private static final String TABLE_ERROR_MESSAGE = "Expected Table Was Not Found";
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;
    @TestData(reference = "variable-name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        WebElement table;
        try {
            table = driver.findElement(element.getBy());
            List<WebElement> allCells = table.findElements(By.tagName("td"));
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(allCells.size()+"");
            setSuccessMessage(String.format(SUCCESS_MESSAGE, variableName.toString()));
            return Result.SUCCESS;
        } catch (Exception exception) {
            exception.printStackTrace();
            setErrorMessage(TABLE_ERROR_MESSAGE);
            return Result.FAILED;
        }

    }
}

