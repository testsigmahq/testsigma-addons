package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Data
@Action(
        actionText = "Verify if testdata is satisfied (Eg: 5 > 3 OR 4 > 5 AND Testsigma CONTAINS sigma)",
        description = "Evaluates arithmetic (+, -, *, /), logical (AND, OR), comparison (>, <, >=, <=, ==, !=), and string conditions (CONTAINS, NOT CONTAINS) dynamically within IF steps.",
        applicationType = ApplicationType.IOS,
        actionType = StepActionType.IF_CONDITION,
        useCustomScreenshot = false
)
public class IfConditionEvaluatorAction extends IOSAction {

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

      boolean finalResult = evaluateCondition(condition);
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

  private boolean evaluateCondition(String condition) throws Exception {

    // Convert logical operators
    condition = condition.replaceAll("(?i)\\bAND\\b", "&&");
    condition = condition.replaceAll("(?i)\\bOR\\b", "||");

    String operandPattern = "([a-zA-Z0-9_\\.]+|\"[^\"]*\"|'[^']*')";

    // Convert NOT CONTAINS
    condition = condition.replaceAll(
            "(?i)" + operandPattern + "\\s+NOT\\s+CONTAINS\\s+" + operandPattern,
            "String($1).indexOf(String($2)) === -1"
    );

    // Convert CONTAINS
    condition = condition.replaceAll(
            "(?i)" + operandPattern + "\\s+CONTAINS\\s+" + operandPattern,
            "String($1).indexOf(String($2)) !== -1"
    );

    logger.info("Converted Condition (JS format): " + condition);

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    if (engine == null) {
      throw new Exception("JavaScript Engine not available. Check Java version.");
    }

    Object evalResult = engine.eval(condition);

    if (evalResult instanceof Boolean) {
      return (Boolean) evalResult;
    }

    if (evalResult instanceof Number) {
      return ((Number) evalResult).doubleValue() != 0;
    }

    return false;
  }
}