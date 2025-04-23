package com.testsigma.addons.web;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute JavaScript snippet testdata on an element1 and store the returned value in variable var1",
        description = "Executes JS and stores the value in runtime variable",
        applicationType = ApplicationType.WEB)
public class ValidateJsValue extends WebAction {

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData jssnippet;

  @Element(reference ="element1")
  private com.testsigma.sdk.Element element;
  
  @TestData(reference = "var1")
  private com.testsigma.sdk.TestData variable;
 
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    //Your Awesome code starts here
    logger.info("Initiating execution");
    logger.info("JS SNIPPET IS "+jssnippet.getValue().toString());

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    try {
    JavascriptExecutor js = (JavascriptExecutor) driver;  
    Object obj = js.executeScript(jssnippet.getValue().toString(),element);
    String str = (obj == null) ? "" : obj.toString();
    
    runTimeData.setKey(variable.getValue().toString());
    runTimeData.setValue(str);
    setSuccessMessage("Successfully stored retured result "+str+" into a runtime variable "+runTimeData);
    }
    catch(Exception e) {
        result = Result.FAILED;
    	logger.debug(e.getMessage());
    	setErrorMessage("Failed to perform operation "+e.getMessage()+e.getCause());
    }
    
     return result;
  }
}