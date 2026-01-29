package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Store testdata_parameter value into a runtime variable Variable_name",
        description = "Stores test data paramter value into runtime",
        applicationType = ApplicationType.REST_API)
public class TestdataToRuntimeRestAPI extends RestApiAction {

  @TestData(reference = "testdata_parameter")
  private com.testsigma.sdk.TestData testdata_parameter_value;
  @TestData(reference = "Variable_name")
  private com.testsigma.sdk.TestData variable_name;
  
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
   
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    if(testdata_parameter_value.getValue().toString()!=null && testdata_parameter_value.getValue().toString().isEmpty()==false &&testdata_parameter_value.getValue().toString().isBlank()==false ) {
    	logger.info("Initiating execution test data parameter contains some value"+testdata_parameter_value.getValue().toString());
    	String sb=testdata_parameter_value.getValue().toString();
    	 runTimeData = new com.testsigma.sdk.RunTimeData();
    	    runTimeData.setValue(sb);
    	    runTimeData.setKey(variable_name.getValue().toString());
    	    setSuccessMessage("Successfully stored the value of parameter::"+testdata_parameter_value.getValue().toString()+"  into runtimevariable  "+variable_name.getValue().toString());
    }
    else {
    	
    	result=com.testsigma.sdk.Result.FAILED;
    	setErrorMessage("Operation failed check if the value of the test data parameter is null/empty/blank");
    }
  
return result;
  }
}
    
    
     
   