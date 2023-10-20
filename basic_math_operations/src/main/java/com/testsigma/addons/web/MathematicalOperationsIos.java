package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Perform mathematical_operations on testdata1 and testdata2 and store the result inside a runtimevariable considering number decimal places",
        description = "Perform math operations and shows the result based on number of decimal places as per users requirement",
        applicationType = ApplicationType.IOS)
public class MathematicalOperationsIos extends MathematicalOperationsWeb {

    
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
    	super.setTestData1(this.testData1);
    	super.setTestData2(this.testData2);
    	super.setTestData3(this.testData3);
    	super.setTestData4(this.testData4);
    	super.setOperator(this.operator);
    	
    
    	return super.execute();
    }
}