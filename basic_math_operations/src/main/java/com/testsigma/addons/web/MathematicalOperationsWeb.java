package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Perform mathematical_operations on testdata1 and testdata2 and store the result inside a runtimevariable considering number decimal places",
        description = "Perform math operations and shows the result based on number of decimal places as per users requirement",
        applicationType = ApplicationType.WEB)
public class MathematicalOperationsWeb extends WebAction {

    
    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "mathematical_operations", allowedValues = {"addition", "subtraction", "multiplication", "division"})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "runtimevariable",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "number")
    private com.testsigma.sdk.TestData testData4;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    

    @Override
    public Result execute() throws NoSuchElementException {
    	
    	com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    	  if(testData1.getValue().toString().isEmpty()|| testData2.getValue().toString().isEmpty()||testData4.getValue().toString().isEmpty()) {
        	  logger.debug("Some data is empty check the source for details");
        	  setErrorMessage("Operation failed. Please check if some datas are empty");
        	  result = Result.FAILED;
        	  
          }
        String operatorString = operator.getValue().toString();
        double a = Double.parseDouble(testData1.getValue().toString());
        double b = Double.parseDouble(testData2.getValue().toString());
        String num=String.valueOf(testData4.getValue().toString());
      
      runTimeData = new com.testsigma.sdk.RunTimeData();
     
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
                 setSuccessMessage("Successfully Performed addition. The sum is "+formattedvaluediff+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                break;
            case "multiplication":
            	double multiplication=a*b;
            	 String formattedvaluemulti=String.format("%."+num+"f",multiplication);
                logger.info("multiplication is "+ formattedvaluemulti);
                runTimeData.setValue(String.valueOf(formattedvaluemulti));
                runTimeData.setKey(testData3.getValue().toString());
                System.out.println(runTimeData);
                setSuccessMessage("Successfully Performed Multiplication. The difference is "+ formattedvaluemulti+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                break;
            case "division":
            	double division=a/b;
            	String formattedvaluediv=String.format("%."+num+"f",division);
                logger.info("division is "+ formattedvaluediv);
                runTimeData.setValue(String.valueOf( formattedvaluediv));
                runTimeData.setKey(testData3.getValue().toString());
                System.out.println(runTimeData);
                setSuccessMessage("Successfully Performed division. The difference is "+ formattedvaluediv+" .:: and has been stored into a runtime variable "+runTimeData.getKey());
                break;
          
        }
       
        return result;
    }
}