package Verify;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the testdata does not contains testdata1",
        description = "Verify that the testdata does not contains testdata",
        applicationType = ApplicationType.WEB)
public class Verifytestdata extends WebAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData1;
  
  @TestData(reference = "testdata1")
  private com.testsigma.sdk.TestData testData2;
  
  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
  
    	logger.debug("testdata => " + testData1.getValue().toString());
        logger.debug("testdata1 => " + testData2.getValue().toString());
         
        String str = testData1.getValue().toString();
 	    
        if (str.contains(testData2.getValue().toString()))
 	    {	
        	 setErrorMessage(testData1.getValue().toString() + " contains " + testData2.getValue().toString());
        	 return Result.FAILED;
        } 
        else 
        {	
        	setSuccessMessage(testData1.getValue().toString() + " does not contains " + testData2.getValue().toString());
        }
 		
    return result;
  }
}