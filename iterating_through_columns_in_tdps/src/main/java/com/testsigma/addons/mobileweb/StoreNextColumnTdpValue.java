package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.TDPApiUtil;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestCaseResult;
import org.openqa.selenium.NoSuchElementException;

import java.util.Map;
import java.util.ArrayList;

@Action(actionText = "store next column name and value from TDP tdp-id for the set name set-name using" +
        " the apikey api-key and iterator TDP_ITERATOR_KEY_NAME in the runtime variables column-name and column-value",
        description = "Store next column name and value from TDP for the given set name using iterator",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class StoreNextColumnTdpValue extends WebAction {

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "set-name")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "api-key")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "column-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData columnNameTestData;
    @TestData(reference = "column-value", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData columnValueTestData;
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
        logger.info("Initiating execution");
        try {
            String tdpId = testData1.getValue().toString();
            String setName = testData2.getValue().toString();
            String apiKey = testData3.getValue().toString();
            Map<String, String> parameterValues = TDPApiUtil.getTDPIterationData(tdpId, setName, apiKey, logger);
            try {
                int iteratorValue = Integer.parseInt(testData5.getValue().toString());
                ArrayList<String> columnValuesList = new ArrayList<>();
                ArrayList<String> columnNamesList = new ArrayList<>();
                for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals("S.No.") || key.equals("ETF") || key.equals("Set Name")) continue;
                    columnValuesList.add(entry.getValue());
                    columnNamesList.add(key);
                }
                if (iteratorValue > columnValuesList.size() - 1) {
                    setErrorMessage("Iterator index " + iteratorValue + " exceeds available columns " + columnValuesList.size());
                    return com.testsigma.sdk.Result.FAILED;
                }
                runTimeData.setValue(columnValuesList.get(iteratorValue));
                runTimeData.setKey(columnValueTestData.getValue().toString());
                columnNameRuntimeData.setValue(columnNamesList.get(iteratorValue));
                columnNameRuntimeData.setKey(columnNameTestData.getValue().toString());
                setSuccessMessage("Successfully retrieved and stored next column data i.e <b>" + columnValuesList.get(iteratorValue) +
                        "</b> for set name: <b>" + setName + "</b>");
                iteratorValue++;
                iteratorRuntimeData.setValue(String.valueOf(iteratorValue));
                iteratorRuntimeData.setKey("TDP_ITERATOR_KEY_NAME");
            } catch (NumberFormatException e) {
                setErrorMessage("Error occurred while parsing iterator value: " + e.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            } catch (Exception e) {
                setErrorMessage("Error occurred while getting iterator value: " + e.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            }
        } catch (Exception e) {
            setErrorMessage("Error occurred while processing TDP data: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
        return com.testsigma.sdk.Result.SUCCESS;
    }
}
