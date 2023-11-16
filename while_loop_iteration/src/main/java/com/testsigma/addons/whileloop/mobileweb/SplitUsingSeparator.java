package com.testsigma.addons.whileloop.mobileweb;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "Iterate SEPARATOR separated values in RUN_TIME_TESTDATA whose runtime variable name is RUN_TIME_TESTDATA_VAR_NAME and store current iteration value in variable VARIABLE_NAME.",
        applicationType = ApplicationType.MOBILE_WEB,
        actionType = StepActionType.WHILE_LOOP,
        description = "This action is applicable only in WHILE Loop condition. In this action we take the data only from Runtime variable as input and split the data at given separator and " +
                "iterate through the res")
public class SplitUsingSeparator extends WebAction {

  @TestData(reference = "SEPARATOR")
  private com.testsigma.sdk.TestData separator;

  @TestData(reference = "RUN_TIME_TESTDATA")
  private com.testsigma.sdk.TestData inputData;

  @TestData(reference = "RUN_TIME_TESTDATA_VAR_NAME",isRuntimeVariable = true)
  private com.testsigma.sdk.TestData runTimeVariableName;
  @TestData(reference = "VARIABLE_NAME",isRuntimeVariable = true)
  private com.testsigma.sdk.TestData currentIterationRuntimeVarName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData currentIterationRunTimeData;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData pendingIterationRunTimeData;

  @Override
  protected Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    Result result = Result.SUCCESS;
    StringBuilder sb = new StringBuilder();
    logger.info("Pending values for iteration:"+inputData.getValue().toString());
    logger.info("Provided Separator:"+separator.getValue().toString());
    if(inputData.getValue().toString().isEmpty() || inputData.getValue().toString().trim().equals(separator.getValue().toString())){
      logger.info("No values to iterate the while loop.");
      setErrorMessage("No values to iterate the while loop");
      return Result.FAILED;
    }
    String[] valuesInArray = inputData.getValue().toString().split(separator.getValue().toString());
    String currentIterationValue = valuesInArray[0];
    if(valuesInArray.length == 1){
      sb.append("");
    }else{
      for(int i=1;i<valuesInArray.length;i++){
        sb.append(valuesInArray[i]);
        if(i != (valuesInArray.length-1)){
           sb.append(separator.getValue().toString());
        }
      }
    }
    logger.info("Stored current iteration value into runtime variable ");
    logger.info(String.format("%s=%s",currentIterationRuntimeVarName.getValue().toString(),currentIterationValue));
    currentIterationRunTimeData = new com.testsigma.sdk.RunTimeData();
    currentIterationRunTimeData.setValue(currentIterationValue);
    currentIterationRunTimeData.setKey(currentIterationRuntimeVarName.getValue().toString());

    logger.info("Pending data to iterate: "+sb.toString());
    logger.info("Setting pending data to runtime variable");
    logger.info(String.format("%s=%s",runTimeVariableName.getValue().toString(),sb.toString()));
    pendingIterationRunTimeData = new com.testsigma.sdk.RunTimeData();
    pendingIterationRunTimeData.setValue(sb.toString());
    pendingIterationRunTimeData.setKey(runTimeVariableName.getValue().toString());
    setSuccessMessage("Successfully executed the condition step. Please check the log for more info on execution");
    return result;
  }
}