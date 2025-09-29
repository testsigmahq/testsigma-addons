package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Perform mathematical_operations on testdata1 and testdata2 and store the result inside a runtimevariable considering number decimal places",
        description = "Perform math operations and shows the result based on number of decimal places as per users requirement",
        applicationType = ApplicationType.WINDOWS)
public class MathematicalOperationsDesktop extends WebAction {

    
    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "mathematical_operations", allowedValues = {"addition", "subtraction", "multiplication", "division"})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "runtimevariable" , isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "number")
    private com.testsigma.sdk.TestData testData4;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData = new com.testsigma.sdk.RunTimeData();
    

    @Override
    public Result execute() throws NoSuchElementException {
    	
    	Result result = Result.SUCCESS;
    	  if(testData1.getValue().toString().isEmpty()|| testData2.getValue().toString().isEmpty()||testData4.getValue().toString().isEmpty()) {
        	  logger.info("Some data is empty check the source for details");
        	  setErrorMessage("Operation failed. Please check if some datas are empty");
        	  result = Result.FAILED;
        	  
          }
          try {
              String operatorString = operator.getValue().toString();
              double a = Double.parseDouble(testData1.getValue().toString().replaceAll("[$,@,%,#]",""));
              double b = Double.parseDouble(testData2.getValue().toString().replaceAll("[$,@,%,#]",""));
              String num=String.valueOf(testData4.getValue().toString());



              switch (operatorString) {
                  case "addition":
                      double sum=a+b;
                      String formattedvaluesum=String.format("%."+num+"f", sum);
                      runTimeData.setValue(String.valueOf(formattedvaluesum));
                      runTimeData.setKey(testData3.getValue().toString());
                      logger.info("sum is "+formattedvaluesum);
                      System.out.println(runTimeData);
                      setSuccessMessage("Successfully Performed addition. The sum is "+formattedvaluesum+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                      break;
                  case "subtraction":
                      double difference=a-b;
                      String formattedvaluediff=String.format("%."+num+"f", difference);
                      runTimeData.setValue(String.valueOf(formattedvaluediff));
                      runTimeData.setKey(testData3.getValue().toString());
                      logger.info("difference is "+formattedvaluediff);
                      System.out.println(runTimeData);
                      setSuccessMessage("Successfully Performed Subtraction. The output is "+formattedvaluediff+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                      break;
                  case "multiplication":
                      double multiplication=a*b;
                      String formattedvaluemulti=String.format("%."+num+"f",multiplication);
                      logger.info("multiplication is "+ formattedvaluemulti);
                      runTimeData.setValue(String.valueOf(formattedvaluemulti));
                      runTimeData.setKey(testData3.getValue().toString());
                      System.out.println(runTimeData);
                      setSuccessMessage("Successfully Performed Multiplication. The output is "+ formattedvaluemulti+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                      break;
                  case "division":
                      double division=a/b;
                      String formattedvaluediv=String.format("%."+num+"f",division);
                      logger.info("division is "+ formattedvaluediv);
                      runTimeData.setValue(String.valueOf( formattedvaluediv));
                      runTimeData.setKey(testData3.getValue().toString());
                      System.out.println(runTimeData);
                      setSuccessMessage("Successfully Performed division. The output is "+ formattedvaluediv+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                      break;

              }
          } catch (Exception e) {
              result = Result.FAILED;
              logger.warn("Exception occurred while performing math operations " + ExceptionUtils.getStackTrace(e));
              setErrorMessage("Exception occurred while performing math operations " + ExceptionUtils.getMessage(e));
          }
       
        return result;
    }
}