package com.testsigma.addons.salesforce;

import com.testsigma.sdk.SalesforceAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(
        actionText = "Verify if testdata1 operations testdata2",
        description = "Verifies whether the condition between test data1 and test data2 is satisfied using one of the available comparison operators",
        applicationType = ApplicationType.Salesforce,
        useCustomScreenshot = false
)
public class CompareValues extends SalesforceAction {

  @TestData(reference = "testdata1")
  private com.testsigma.sdk.TestData testData1;

  @TestData(reference = "testdata2")
  private com.testsigma.sdk.TestData testData2;

  @TestData(reference = "operations", allowedValues = {"==", ">", ">=", "LESSTHAN", "<=", "CONTAINS", "!="})
  private com.testsigma.sdk.TestData operation;

  private static final String SUCCESS_MESSAGE = "\"%s %s %s\" is evaluated as true";
  private static final String FAILURE_MESSAGE = "\"%s %s %s\" is evaluated as false";

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    String testdata1 = testData1.getValue().toString().trim();
    String testdata2 = testData2.getValue().toString();
    String operations = operation.getValue().toString().trim();

    try {
      boolean expressionResult = evaluateExpression(testdata1, operations, testdata2);

      if (!expressionResult) {
        result = com.testsigma.sdk.Result.FAILED;
        logger.warn(String.format(FAILURE_MESSAGE, testdata1, operations, testdata2));
        setErrorMessage(String.format(FAILURE_MESSAGE, testdata1, operations, testdata2));
      } else {
        setSuccessMessage(String.format(SUCCESS_MESSAGE, testdata1, operations, testdata2));
      }

    } catch (Exception e) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Exception: " + ExceptionUtils.getMessage(e));
    }

    return result;
  }

  private boolean evaluateExpression(String testData1, String operation, String testData2) throws IllegalArgumentException {
    String op = operation.trim().toLowerCase();

    switch (op) {
      case ">=":
        return compareGreaterThanOrEqual(testData1, testData2);
      case ">":
        return compareGreaterThan(testData1, testData2);
      case "<=":
        return compareLessThanOrEqual(testData1, testData2);
      case "lessthan":
      case "<":
        return compareLessThan(testData1, testData2);
      case "==":
        return compareEqual(testData1, testData2);
      case "!=":
        return compareNotEqual(testData1, testData2);
      case "contains":
        return testData1.contains(testData2);
      default:
        throw new IllegalArgumentException("Unsupported operation: " + operation);
    }
  }

  private static boolean compareGreaterThanOrEqual(String testData1, String testData2) {
    if (isNumeric(testData1) && isNumeric(testData2)) {
      return Double.parseDouble(testData1) >= Double.parseDouble(testData2);
    } else {
      return testData1.compareTo(testData2) >= 0;
    }
  }

  private static boolean compareGreaterThan(String testData1, String testData2) {
    if (isNumeric(testData1) && isNumeric(testData2)) {
      return Double.parseDouble(testData1) > Double.parseDouble(testData2);
    } else {
      return testData1.compareTo(testData2) > 0;
    }
  }

  private static boolean compareLessThanOrEqual(String testData1, String testData2) {
    if (isNumeric(testData1) && isNumeric(testData2)) {
      return Double.parseDouble(testData1) <= Double.parseDouble(testData2);
    } else {
      return testData1.compareTo(testData2) <= 0;
    }
  }

  private static boolean compareLessThan(String testData1, String testData2) {
    if (isNumeric(testData1) && isNumeric(testData2)) {
      return Double.parseDouble(testData1) < Double.parseDouble(testData2);
    } else {
      return testData1.compareTo(testData2) < 0;
    }
  }

  private static boolean compareEqual(String testData1, String testData2) {
    if (isNumeric(testData1) && isNumeric(testData2)) {
      return Double.parseDouble(testData1) == Double.parseDouble(testData2);
    } else {
      return testData1.equals(testData2);
    }
  }

  private static boolean compareNotEqual(String testData1, String testData2) {
    if (isNumeric(testData1) && isNumeric(testData2)) {
      return Double.parseDouble(testData1) != Double.parseDouble(testData2);
    } else {
      return !testData1.equals(testData2);
    }
  }

  private static boolean isNumeric(String str) {
    return str != null && str.matches("^[0-9]+(\\.[0-9]+)?$");
  }
}
