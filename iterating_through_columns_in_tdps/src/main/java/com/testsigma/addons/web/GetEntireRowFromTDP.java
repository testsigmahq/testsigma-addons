package com.testsigma.addons.web;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import org.openqa.selenium.NoSuchElementException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "get entire row from TDP tdp-id for the set name set-name using the apikey api-key and" +
        " store data in the run time variable runtime-variable",
        description = "Get entire row from TDP",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class GetEntireRowFromTDP extends WebAction {
    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData4;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        logger.debug("TDP ID: "+ this.testData1.getValue() +", Set Name: "+ this.testData2.getValue() +", API Key: "+ this.testData3.getValue());
        
        try {
            String tdpId = testData1.getValue().toString();
            String setName = testData2.getValue().toString();
            String apiKey = testData3.getValue().toString();
            
            // Use the utility class to get TDP iteration data
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpId, setName, apiKey);
            
            // Log the retrieved parameter values
            parameterValues.forEach((key, value) -> 
                logger.info("Stored parameter - " + key + ": " + value)
            );
            
            // Store individual parameter values as runtime data

            // 
            String resultVariableForSToringOutput ="";

            
            // Loop through the parameters/columns in the parameterValues map
            for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                // skip the first three columns
                
                String key = entry.getKey();
                if (key.equals("S.No.") || key.equals("ETF") || key.equals("Set Name")) {
                    logger.info("Skipping column: " + key);
                    continue;
                }
                else{
                    resultVariableForSToringOutput += entry.getValue() + ", ";
                }
            }
            // remove the last comma
            resultVariableForSToringOutput = resultVariableForSToringOutput.substring(0, resultVariableForSToringOutput.length() - 2);
     
                runTimeData.setValue(resultVariableForSToringOutput);
                runTimeData.setKey(testData4.getValue().toString());
                logger.info("Stored first parameter in main runtime variable: " + resultVariableForSToringOutput);
            
            logger.info("Successfully retrieved and stored data for iteration: " + setName);
            return com.testsigma.sdk.Result.SUCCESS;
            
        } catch (Exception e) {
            logger.warn("Error occurred while processing TDP data: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}