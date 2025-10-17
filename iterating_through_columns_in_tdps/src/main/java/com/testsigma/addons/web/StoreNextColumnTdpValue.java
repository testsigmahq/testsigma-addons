package com.testsigma.addons.web;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import org.openqa.selenium.NoSuchElementException;

import com.testsigma.sdk.annotation.TestCaseResult;

import java.util.Map;
import java.util.ArrayList;


@Action(actionText = "store next column name and value from TDP tdp-id for the set name set-name using" +
        " the apikey api-key and iterator TDP_ITERATOR_KEY_NAME in the runtime variables column-name and column-value",
        description = "Store next column name and value from TDP tdp-id for the set name set-name using the apikey" +
                " api-key and iterator TDP_ITERATOR_KEY_NAME in the runtime variable runtime-variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreNextColumnTdpValue extends WebAction {
    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "column-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData4;
    @TestData(reference = "column-value", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData6;
    @TestData(reference = "TDP_ITERATOR_KEY_NAME", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData5;
    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;
    @RunTimeData
    private static com.testsigma.sdk.RunTimeData runTimeData;
    @RunTimeData
    private static com.testsigma.sdk.RunTimeData iteratorRuntimeData;
    @RunTimeData
    private static com.testsigma.sdk.RunTimeData columnNameRuntimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution manoj");
        try {
            String tdpId = testData1.getValue().toString();
            String setName = testData2.getValue().toString();
            String apiKey = testData3.getValue().toString();
            logger.debug("TDP ID: " + tdpId + ", Set Name: " + setName + ", API Key: " + apiKey);
            // use api util to get the next column value
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpId, setName, apiKey);

            // get the iterator value
            try {
                int iteratorValue = Integer.parseInt(testData5.getValue().toString());
                logger.info("Iterator value: " + iteratorValue);
                ArrayList<String> resultVariableForStoringOutput = new ArrayList<>();
                ArrayList<String> columnNamesList = new ArrayList<>();
                // Loop through the parameters/columns in the parameterValues map
                for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                    // skip the first three columns
                    String key = entry.getKey();
                    if (key.equals("S.No.") || key.equals("ETF") || key.equals("Set Name")) {
                        continue;
                    } else {
                        logger.info("Adding column value to list: " + entry.getValue());
                        resultVariableForStoringOutput.add(entry.getValue());
                        columnNamesList.add(key);
                    }
                }
                logger.info("Total columns available after skipping first three columns: " + resultVariableForStoringOutput.size());
                if (iteratorValue > resultVariableForStoringOutput.size() -1) {
                    logger.info("Iterator index " + iteratorValue + " exceeds available columns " + resultVariableForStoringOutput.size());
                    setErrorMessage("Iterator index " + iteratorValue + " exceeds available columns " + resultVariableForStoringOutput.size());
                    return com.testsigma.sdk.Result.FAILED;
                }

                logger.info("Storing column value at index " + iteratorValue + " to runtime variable = "
                        + resultVariableForStoringOutput.get(iteratorValue));
                runTimeData.setValue(resultVariableForStoringOutput.get(iteratorValue));
                runTimeData.setKey(testData4.getValue().toString());

                // store the key value of result variable
                columnNameRuntimeData.setValue(columnNamesList.get(iteratorValue));
                columnNameRuntimeData.setKey(testData6.getValue().toString());
                logger.info("Stored column name in runtime variable: " + columnNamesList.get(iteratorValue));
                logger.info("Stored next column value in runtime variable: " + resultVariableForStoringOutput.get(iteratorValue));

                setSuccessMessage("Successfully retrieved and stored next column data i.e <b>" + resultVariableForStoringOutput.get(iteratorValue) +
                        "</b> for set name: <b>" + setName + "</b>");

                // updating the iterator value for next iteration...
                iteratorValue++;
                iteratorRuntimeData.setValue(String.valueOf(iteratorValue));
                iteratorRuntimeData.setKey("TDP_ITERATOR_KEY_NAME");
                logger.info("Incremented iterator value: " + iteratorValue);

            } catch (NumberFormatException e) {
                logger.info("Error occurred while parsing iterator value: " + e.getMessage());
                setErrorMessage("Error occurred while parsing iterator value: " + e.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            } catch (Exception e) {
                logger.info("Error occurred while getting iterator value: " + e.getMessage());
                setErrorMessage("Error occurred while getting iterator value: " + e.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("Error occurred while processing TDP data: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
        return com.testsigma.sdk.Result.SUCCESS;

    }
}