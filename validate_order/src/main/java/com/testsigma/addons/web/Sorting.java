package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

@Data
@Action(actionText = "Verify if the column data is in Ascending/Descending order",
        description = "Verifies if the data inside the column is in ascending or descending order",
        applicationType = ApplicationType.WEB)
public class Sorting extends WebAction {

   
    @TestData(reference = "Ascending/Descending", allowedValues = {"ascending", "descending"})
    private com.testsigma.sdk.TestData operator;
    @Element(reference = "column")
    private com.testsigma.sdk.Element tableElement;
   
    

    @Override
    public Result execute() throws NoSuchElementException {
    	
    	com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;  
        String operatorString = operator.getValue().toString();
        switch (operatorString) {
            case "ascending":
            	
            	 try {
                 	
                	 List<WebElement> wb=driver.findElements(tableElement.getBy());   //sorted
                     List<String> aftersort = new ArrayList<String>();
                     for(WebElement e : wb){
                         aftersort.add(e.getText());
                     }
                    
                     Collections.sort(aftersort);
                     System.out.println(aftersort);
                     //actual
                     List<String> beforesort = new ArrayList<String>();
                     for(WebElement e : wb){
                         beforesort.add(e.getText());
                     }
                     Assert.assertEquals(aftersort,beforesort);
                     logger.info(" After sort data  "+aftersort);
                     logger.info(" Before sort data  "+beforesort);
                     
                     setSuccessMessage("Assertion passed,the column is in ascending order");
                   
                	
                }catch (Exception e) {
                	e.printStackTrace();
                	logger.debug(e.getMessage()+e.getCause());
                	result = com.testsigma.sdk.Result.FAILED;
                	setErrorMessage("Sorting Operation failed. Please check if the column is sorted in ascending");
                	
                	
                } 
             
                break;
            case "descending":
            	  try {
                  	
                 	 List<WebElement> wb=driver.findElements(tableElement.getBy());   //sorted
                      List<String> aftersort = new ArrayList<String>();
                      for(WebElement e : wb){
                          aftersort.add(e.getText());
                      }
                     
                      Collections.sort(aftersort);
                      Collections.reverse(aftersort);
                      
                      
                      List<String> beforesort = new ArrayList<String>();
                      for(WebElement e : wb){
                          beforesort.add(e.getText());
                      }
                      Assert.assertEquals(aftersort,beforesort);
                      logger.info(" After sort data  "+aftersort);
                      logger.info(" Before sort data  "+beforesort);
                      
                      setSuccessMessage("Assertion passed,the column is in descending order");
                    
                 	
                 }catch (Exception e) {
                 	e.printStackTrace();
                 	logger.debug(e.getMessage()+e.getCause());
                 	result = com.testsigma.sdk.Result.FAILED;
                 	setErrorMessage("Sorting Operation failed. Please check if the column is sorted in descending");
                 		
                 } 
                break;
          
        }
       
        return result;
    }
}