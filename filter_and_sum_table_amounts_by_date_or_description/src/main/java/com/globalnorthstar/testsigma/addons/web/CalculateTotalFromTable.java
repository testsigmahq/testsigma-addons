package com.globalnorthstar.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;

@Data
@Action(
        actionText = "From table element-locator, calculate the total amount for rows where date is filter-date or description is filter-description and store it in a runtime variable variable-name",
        description = "Filters a table by the given date or description, sums the 'Amount' column for all matching rows, and saves the final result into a runtime variable.",
        applicationType = ApplicationType.WEB
)
public class CalculateTotalFromTable extends WebAction {

  @Element(reference = "element-locator")
  private com.testsigma.sdk.Element tableElement;

  @TestData(reference = "filter-date")
  private com.testsigma.sdk.TestData filterDate;

  @TestData(reference = "filter-description")
  private com.testsigma.sdk.TestData filterDescription;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData runtimeVar;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    logger.info("Initiating action: CalculateTotalFromTable");
    Result result = Result.SUCCESS;
    double totalAmount = 0.0;

    try {
      logger.info("Table locator: " + tableElement.getElement());
      logger.info("Filter Date: " + filterDate.getValue());
      logger.info("Filter Description: " + filterDescription.getValue());

      WebElement table = tableElement.getElement();
      logger.info("Table element fetched successfully");

      List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));

      logger.info("Row count: " + rows.size());

      if (rows.isEmpty()) {
        logger.warn("No rows were found in the specified table. The calculation will result in 0.");
      }

      for (WebElement row : rows) {
        List<WebElement> cells = row.findElements(By.tagName("td"));

        if (cells.size() >= 3) {
          String date = cells.get(0).getText();
          String description = cells.get(1).getText();

          if (Objects.equals(filterDate.getValue().toString(), date) || Objects.equals(filterDescription.getValue().toString(), description)) {
            String amountString = cells.get(2).getText();
            double amount = parseAmount(amountString);
            totalAmount += amount;
            logger.info("MATCHED: Date='" + date + "', Description='" + description + "'. Adding amount: " + amount + ". New total: " + totalAmount);
          }
        }
      }

      String formattedTotal = String.format("%.2f", totalAmount);

      runTimeData.setValue(formattedTotal);
      runTimeData.setKey(runTimeData.getValue().toString());

      setSuccessMessage("Successfully calculated the total amount as " + formattedTotal +
              " and stored it in the runtime variable '" + runtimeVar.getValue().toString() + "'.");

    } catch (Exception e) {
      logger.warn("An unexpected error occurred: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Failed to calculate total from table. Error: " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }

    return result;
  }

  private double parseAmount(String amountString) {
    if (amountString == null || amountString.trim().isEmpty()) {
      logger.warn("Amount string is empty or null, returning 0.0");
      return 0.0;
    }
    String cleanAmount = amountString.replace("(", "").replace(")", "").replace("$", "").trim();
    try {
      return Double.parseDouble(cleanAmount);
    } catch (NumberFormatException e) {
      logger.warn("Could not parse '" + amountString + "' as a number. Returning 0.0 for this amount." + e);
      return 0.0;
    }
  }
}