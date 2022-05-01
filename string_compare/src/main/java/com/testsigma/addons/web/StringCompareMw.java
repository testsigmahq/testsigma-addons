package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if string1 matches with string2",
        description = "Verify if both the string matches",
        applicationType = ApplicationType.MOBILE_WEB)
public class StringCompareMw extends WebAction {

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
    
  
    	if(str1.equals(str2)) {
    		logger.info("Both Strings matches");
    		setSuccessMessage("Both the string matches "+str1+" = "+str2);
    		System.out.println("Both the string matches "+str1+" = "+str2);
    		 result = com.testsigma.sdk.Result.SUCCESS;
    	}
    	else {
        	result = com.testsigma.sdk.Result.FAILED;
        	logger.debug("donne");
        	setErrorMessage("Operation failed strings doesnot match");
        	
    	}
    	
   
  
   
    return result;
  }
}