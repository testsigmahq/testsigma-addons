package com.testsigma.addons.restapi;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Action(actionText = "Convert the date from the testdata in format1 to format2 and store it in a runtime variable",
        description = "To store the number of char from testdata into runtime variable",
        applicationType = ApplicationType.REST_API)

public class DateFormatConversion extends RestApiAction
{

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData1;
    
    @TestData(reference = "format1")
    private com.testsigma.sdk.TestData testData2;
    
    @TestData(reference = "format2")
    private com.testsigma.sdk.TestData testData3;
    
    @TestData(reference = "variable")
    private com.testsigma.sdk.TestData runtimeVar;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException
    {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        Result result = Result.SUCCESS;
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(testData2.getValue().toString());
            SimpleDateFormat outputFormat = new SimpleDateFormat(testData3.getValue().toString());
        	logger.info("testdata = " + testData1.getValue().toString());
        	logger.info("format1 = " + testData2.getValue().toString());
        	logger.info("format2 = " + testData3.getValue().toString());
        	logger.info("variable = " + runtimeVar.getValue().toString());
        	
        	String inputDateString = testData1.getValue().toString();
            Date parsedDate = inputFormat.parse(inputDateString);
            
            String outputDateString = outputFormat.format(parsedDate);
            logger.info("outputDateString = " + outputDateString);
        	
            runTimeData.setKey(runtimeVar.getValue().toString());
            runTimeData.setValue(outputDateString);

            logger.info("Successfully converted date from " + testData2.getValue().toString() + " to " + testData3.getValue().toString() + " and stored in runtime variable = " +outputDateString);
            setSuccessMessage("Successfully converted date from " + testData2.getValue().toString() + " to " + testData3.getValue().toString() + " and stored in runtime variable = " +outputDateString);
        } catch(Exception e) {
            logger.warn("Operation failed , the error message is ::::"+ ExceptionUtils.getStackTrace(e));
            setErrorMessage("Operation failed , the error message is ::::"+ ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }
		return result;
    }
}

