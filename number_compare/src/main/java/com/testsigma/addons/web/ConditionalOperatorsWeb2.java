package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if test-data-1 operator test-data-2",
        description = "This is a numeric operation that finds relation between two numeric value",
        applicationType = ApplicationType.WEB)
public class ConditionalOperatorsWeb2 extends WebAction {

   
    @TestData(reference = "test-data-1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "operator", allowedValues = {">", "<", ">=", "<=", "=="})
    private com.testsigma.sdk.TestData operator;
    @TestData(reference = "test-data-2")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public Result execute() throws NoSuchElementException {
        String operatorString = operator.getValue().toString();
        double a = Double.valueOf(testData1.getValue().toString());
        double b = Double.valueOf(testData2.getValue().toString());
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        switch (operatorString) {
            case ">":
            	if(a>b) {
            		setSuccessMessage("Passed "+a+" is > "+b);
            		System.out.print("Success");		
            	}
            	else {
            		result=com.testsigma.sdk.Result.FAILED;
            		setErrorMessage("Validation failed");
            		System.out.println("failure");
            	}
               
                break;
            case "<":
            	if(a<b) {
            		setSuccessMessage("Passed "+a+" is < "+b);
            		System.out.print("Success");		
            	}
            	else {
            		result=com.testsigma.sdk.Result.FAILED;
            		setErrorMessage("Validation failed");
            		System.out.println("failure");
            	}
                
                break;
            case ">=":
            	if(a>=b) {
            		setSuccessMessage("Passed "+a+" is >= "+b);
            		System.out.print("Success");		
            	}
            	else {
            		result=com.testsigma.sdk.Result.FAILED;
            		setErrorMessage("Validation failed");
            		System.out.println("failure");
            	}
               
                break;
            case "<=":
            	if(a<=b) {
            		setSuccessMessage("Passed "+a+" is <= "+b);
            		System.out.print("Success");		
            	}
            	else {
            		result=com.testsigma.sdk.Result.FAILED;
            		setErrorMessage("Validation failed");
            		System.out.println("failure");
            	}
                
                break;
            case "==":
            	if(a==b) {
            		setSuccessMessage("Passed "+a+" is == "+b);
            		System.out.print("Success");		
            	}
            	else {
            		result=com.testsigma.sdk.Result.FAILED;
            		setErrorMessage("Validation failed");
            		System.out.println("failure");
            	}
               
                break;
        }
       
           
        return result;
    }
}
