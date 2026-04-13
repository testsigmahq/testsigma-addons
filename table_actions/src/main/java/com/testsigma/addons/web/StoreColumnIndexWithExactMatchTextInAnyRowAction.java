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
@Action(actionText = "Store the column number in the table element-locator which has the exact text test-data in ANY row into the variable variable-name",
        description = "Stores the column number in a table element that contains the text in any row into runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreColumnIndexWithExactMatchTextInAnyRowAction extends WebAction {

  @TestData(reference = "test-data")
  private com.testsigma.sdk.TestData text;

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
      int columnIndex = getColumnIndexOfText(text.getValue().toString(), element.getElement());
      runTimeData.setValue(String.valueOf(columnIndex));
      runTimeData.setKey(variableName.getValue().toString());
      setSuccessMessage("Column number stored successfully in variable: " + variableName.getValue().toString() + " . " + variableName.getValue().toString() + " = " + columnIndex);
    } catch (Exception e) {
      logger.warn("Error occurred while executing action: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Error occurred while executing action: " + ExceptionUtils.getStackTrace(e));
      return com.testsigma.sdk.Result.FAILED;
    }
    return result;
  }

  private int getColumnIndexOfText(String text, WebElement table) throws Exception {
    List<WebElement> rows = TableActionUtils.getRows(table);
    for (WebElement row : rows) {
      List<WebElement> cells = TableActionUtils.getCellsOfRow(row);
      for (int j = 0; j < cells.size(); j++) {
        if (cells.get(j).getText().trim().equals(text.trim())) {
          return j + 1;
        }
      }
    }
    throw new Exception("Column with specified text is not found in given table");
  }
}
