package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
@Action(actionText = "Store the row number in the table element-locator which has the exact text test-data in column column-name into the variable variable-name",
        description = "Stores the row number in a table element that contains the text in the specified column into runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreRowIndexWithExactMatchInExactColumnTextAction extends WebAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData text;
  
  @TestData(reference = "column-name")
  private com.testsigma.sdk.TestData columnName;
  
  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;
  
  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element element;
  
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    try {
        int rowIndex = getRowIndexOfTextInColumn(text.getValue().toString(), columnName.getValue().toString(), element.getElement());
        runTimeData.setValue(String.valueOf(rowIndex));
        runTimeData.setKey(variableName.getValue().toString());
        setSuccessMessage("Row number stored successfully in variable: " + variableName.getValue().toString() + " . " + variableName.getValue().toString() + " = " + rowIndex);
    } catch (Exception e) {
      logger.warn("Error occurred while executing action: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Error occurred while executing action: " + ExceptionUtils.getStackTrace(e));
      return com.testsigma.sdk.Result.FAILED;
    }
    return result;
  }

  private int getRowIndexOfTextInColumn(String text, String columnName, WebElement table) throws Exception {
    int columnIndex = TableActionUtils.getColumnIndex(columnName, table);
    List<WebElement> rows = TableActionUtils.getRows(table);
    for (int i = 0; i < rows.size(); i++) {
      WebElement cell = TableActionUtils.getCellsOfRow(rows.get(i)).get(columnIndex - 1);
      if (cell.getText().trim().equals(text.trim())) {
        return i + 1;
      }
    }
    throw new Exception("Row with specified text is not found in given table");
  }
}