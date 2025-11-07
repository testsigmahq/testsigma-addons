package com.testsigma.addons.windows;

import com.testsigma.addons.utils.JSONUtilities;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(
        actionText = "fetch the data from the json file file-path and store it in the runtime variable variable-name",
        description = "This action stores JSON data into a runtime variable.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS
)
public class StoreJSONDataIntoRuntimeVariable extends WebAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        try {
            com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
            String inputFilePath = filePath.getValue().toString();
            
            try {
                JSONUtilities jsonUtilities = new JSONUtilities(logger);
                // Use utils to read JSON from file
                String jsonData = jsonUtilities.readJsonFromFile(inputFilePath, logger);
                
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(jsonData);
                logger.info("Stored JSON data in runtime variable: " + variableName.getValue().toString() +
                        " = " + jsonData);
                setSuccessMessage("Successfully stored JSON data in runtime variable: " +
                        variableName.getValue().toString() + " = " + jsonData);
                return result;
            } catch (java.io.FileNotFoundException e) {
                String errorMsg = "File not found: " + inputFilePath + ". Please verify the file path.";
                logger.warn(errorMsg);
                setErrorMessage(errorMsg);
                return com.testsigma.sdk.Result.FAILED;
            } catch (java.io.IOException e) {
                String errorMsg = "Failed to read JSON file: " + inputFilePath + " - " + e.getMessage();
                logger.warn(errorMsg);
                setErrorMessage(errorMsg);
                return com.testsigma.sdk.Result.FAILED;
            } catch (Exception e) {
                String errorMsg = "Error processing JSON file: " + inputFilePath + " - " + e.getMessage();
                logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
                setErrorMessage(errorMsg);
                return com.testsigma.sdk.Result.FAILED;
            }
        } catch (Exception e) {
            String errorMsg = "Failed to store JSON data into runtime variable: " + e.getMessage();
            logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(errorMsg);
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}

