package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Trim white space from testdata and store it in a runtime variable",
        description = "This addon will trim all the white spaces from the given string and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB)

public class TrimWhiteSpaces extends WebAction {
	
  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testdata;
  
  @TestData(reference = "variable", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData runtimeVar;
  
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    Result result = Result.SUCCESS;
    try 
    {
    	String input = testdata.getValue().toString();
    	if (input.isEmpty()) 
    	{
    		result = Result.FAILED;
    		setErrorMessage("Given testdata is empty");
			
		} else 
		{
			String Output = input.replaceAll("\\s", "");
            logger.info("Output: " + Output);
    	    
    		runTimeData.setKey(runtimeVar.getValue().toString());
    		runTimeData.setValue(Output);
           
    		result = Result.SUCCESS;
    		setSuccessMessage("Successfully trimmed the whitespace from the given string and store in a runtime variable " + runtimeVar.getValue().toString() +" = "+ Output);
		}
    } catch (Exception e) {
        result = Result.FAILED;
        logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
        setErrorMessage("Exception Occurred: " + ExceptionUtils.getMessage(e));
	}
    return result;
  }
}