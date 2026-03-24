package com.testsigma.addons.windows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.addons.utils.ConditionEvaluatorUtils;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if testdata is satisfied (Eg: 5 > 3 OR 4 > 5 AND Testsigma CONTAINS sigma; X CONTAINS (A OR B) or X CONTAINS (A AND B))", description = "Evaluates arithmetic (+, -, *, /), logical (AND, OR), comparison (>, <, >=, <=, ==, !=), and string conditions (CONTAINS, NOT CONTAINS) dynamically within IF steps. CONTAINS (A OR B) expands to CONTAINS A OR CONTAINS B; CONTAINS (A AND B) expands to CONTAINS A AND CONTAINS B.", applicationType = ApplicationType.WINDOWS, actionType = StepActionType.IF_CONDITION, useCustomScreenshot = false)
public class IfConditionEvaluatorAction extends WindowsAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {

    logger.info("===== Starting Condition Evaluation =====");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    try {

      if (testData == null || testData.getValue() == null) {
        throw new Exception("Condition cannot be empty");
      }

      String condition = testData.getValue().toString().trim();
      logger.info("Original Condition: " + condition);
      logger.info("Converted Condition (JS format): " + ConditionEvaluatorUtils.toJavaScriptExpression(condition));
      boolean finalResult = ConditionEvaluatorUtils.evaluateCondition(condition);
      logger.info("Final Evaluated Result: " + finalResult);

      if (!finalResult) {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("Condition evaluated to FALSE");
        return result;
      }

      setSuccessMessage("Condition evaluated to TRUE");

    } catch (Exception e) {
      result = com.testsigma.sdk.Result.FAILED;
      logger.warn("Exception Occurred: " + e.getMessage());
      setErrorMessage("Exception Occurred: " + e.getMessage());
    }

    return result;
  }

}