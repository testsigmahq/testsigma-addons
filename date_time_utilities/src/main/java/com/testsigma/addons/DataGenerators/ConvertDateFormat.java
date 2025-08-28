package com.testsigma.addons.DataGenerators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "Convert date format",
        description = "Converts date from one format to another format")
public class ConvertDateFormat extends TestDataFunction {

    @TestDataFunctionParameter(reference = "inputDate")
    private com.testsigma.sdk.TestDataParameter inputDate;
    
    @TestDataFunctionParameter(reference = "inputFormat")
    private com.testsigma.sdk.TestDataParameter inputFormat;
    
    @TestDataFunctionParameter(reference = "outputFormat")
    private com.testsigma.sdk.TestDataParameter outputFormat;

    @Override
    public TestData generate() throws Exception {
        // Try use of run time data
        logger.info("Initiating date format conversion");
        
        try {
            logger.debug("inputDate = " + inputDate.getValue().toString());
            logger.debug("inputFormat = " + inputFormat.getValue().toString());
            logger.debug("outputFormat = " + outputFormat.getValue().toString());
            
            String inputDateString = inputDate.getValue().toString();
            String inputFormatString = inputFormat.getValue().toString();
            String outputFormatString = outputFormat.getValue().toString();
            
            SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormatString);
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormatString);
            
            Date parsedDate = inputDateFormat.parse(inputDateString);
            String convertedDate = outputDateFormat.format(parsedDate);
            
            logger.info("Successfully converted date from " + inputFormatString + " to " + outputFormatString + ": " + convertedDate);
            
            TestData testData = new TestData(convertedDate);
            return testData;
            
        } catch (Exception e) {
            logger.info("Error converting date format: " + e.getMessage());
            throw new Exception("Failed to convert date format: " + e.getMessage());
        }
    }
}
