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

import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Store the ALL row numbers in the table element-locator which has the exact text test-data in ANY column into the variable variable-name",
        description = "Stores all row numbers in a table element that contains the text in any column into runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreAllRowIndexesWithExactMatchTextInAnyColumnAction extends WebAction {

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
        List<Integer> rowIndexes = getAllRowIndexesOfText(text.getValue().toString(), element.getElement());
        String runTimeVarValue = rowIndexes.toString().replaceAll("\\[|\\]", "");
        runTimeData.setValue(runTimeVarValue);
        runTimeData.setKey(variableName.getValue().toString());
        setSuccessMessage("Row number stored successfully in variable: " + variableName.getValue().toString() + " . " + variableName.getValue().toString() + " = " + runTimeVarValue);
    } catch (Exception e) {
      logger.warn("Error occurred while executing action: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Error occurred while executing action: " + ExceptionUtils.getStackTrace(e));
      return com.testsigma.sdk.Result.FAILED;
    }
    return result;
  }

  private List<Integer> getAllRowIndexesOfText(String text, WebElement table) throws Exception {
    List<WebElement> rows = TableActionUtils.getRows(table);
    List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i < rows.size(); i++) {
      List<WebElement> cells = TableActionUtils.getCellsOfRow(rows.get(i));
      for (WebElement cell : cells) {
        if (cell.getText().trim().equals(text.trim())) {
          indexes.add(i + 1);
          break;
        }
      }
    }
    if (indexes.isEmpty()) {
      throw new Exception("Row with specified text is not found in given table");
    } else {
      return indexes;
    }
  }
}