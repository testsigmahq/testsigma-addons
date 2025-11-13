package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.devtools.v135.io.IO;

@Data
@Action(actionText = "Remove Special characters using delimiter testdata1 from testdata2 and store it in runtime variable variable-name",
        description = "Remove special character and store in a runtime variable",
        applicationType = ApplicationType.IOS)
public class RemoveSpecialCharacters extends IOSAction {

    @TestData(reference = "testdata1")
    private com.testsigma.sdk.TestData testData1;
 
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
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
            logger.debug("testdata1 => " + testData1.getValue().toString());
            logger.debug("testdata2 => " + testData2.getValue().toString());
            
            String regex = testData1.getValue().toString();
    		String data = testData2.getValue().toString(); 
    		String RES = data.replaceAll("[" + regex + "]", "");

            runTimeData.setKey(runtimeVar.getValue().toString());
            runTimeData.setValue(String.valueOf(RES));

            logger.info("Successfully removed special characters from " + data + " and stored the Data in runtime variable => " + runtimeVar.getValue().toString( ) + " = " + RES);
            setSuccessMessage("Successfully removed special characters from " + data + " and stored the Data in runtime variable => " + runtimeVar.getValue().toString() + " = " + RES);

        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Operation failed , the error message is "+ ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }
        return result;
    }
}

