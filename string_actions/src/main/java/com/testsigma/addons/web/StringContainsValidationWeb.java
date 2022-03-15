package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if string1 contains string2",
        description = "Verify if one string contains other(Case Insensitive)",
        applicationType = ApplicationType.WEB)
public class StringContainsValidationWeb extends WebAction {

  @TestData(reference = "string1")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "string2")
  private com.testsigma.sdk.TestData testData2;
  
  
  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    logger.debug(" test-data1: "+ this.testData1.getValue()+"test data2:"+this.testData2.getValue());
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    
    String str1=String.valueOf(testData1.getValue());
    String str2=String.valueOf(testData2.getValue());
    
  
    	if(str1.toLowerCase().contains(str2.toLowerCase())) {
    		logger.info("String 1 Contains String 2");
    		setSuccessMessage("Successfully executed  "+str1+"  contains "+str2);
    		//System.out.println("Successfully executed  "+str1+"  contains "+str2);
    		 result = com.testsigma.sdk.Result.SUCCESS;
    	}
    	else {
        	result = com.testsigma.sdk.Result.FAILED;
        	logger.debug("donne");
        	setErrorMessage("Assertion failed "+str1+" doesnot contain "+str2);
        	
    	}
    return result;
  }
}
