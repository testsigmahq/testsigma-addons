package com.testsigma.addons.restapi;

import com.jayway.jsonpath.PathNotFoundException;
import com.testsigma.addons.utils.JSONUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Extract text from a JSON text test-data1 using the specified JSON path test-data2 " +
        "and store it in a runtime variable variable_name",
        description = "Extract required json data using JSON string and JSON path, store data into a runtime variable",
        applicationType = ApplicationType.REST_API)
public class ExtractTextUsingJsonPathInJson extends RestApiAction {

    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData jsonText;

    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData jsonpath;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable_name;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        try {
            Result result = Result.SUCCESS;
            String jsonString = jsonText.getValue().toString();
            logger.info("JSON Data: " + jsonString);

            String jsonPath = jsonpath.getValue().toString();
            logger.info("JSON Path: " + jsonPath);

            try {
                JSONUtilities jsonUtilities = new JSONUtilities(logger);
                String output = jsonUtilities.readJsonData(jsonString, jsonPath, logger);
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(output);
                runTimeData.setKey(variable_name.getValue().toString());
                setSuccessMessage("Stored the desired data into the runtime variable. " + variable_name.getValue().toString() +
                        " = " + runTimeData.getValue());
                logger.info("Extracted Output: " + output);
            } catch (PathNotFoundException e) {
                String errorMsg = "Invalid JSON Path: " + jsonPath + ". Please verify the path.";
                setErrorMessage(errorMsg);
                logger.warn(errorMsg);
                return Result.FAILED;
            } catch (Exception e) {
                String errorMsg = "An unexpected error occurred while extracting JSON data: " + ExceptionUtils.getMessage(e);
                setErrorMessage(errorMsg);
                logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
                return Result.FAILED;
            }
            return result;
        } catch (NullPointerException e) {
            String errorMsg = "Null value encountered in test data or JSON path. Please verify all required fields are provided.";
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
            return Result.FAILED;
        } catch (Exception e) {
            String errorMsg = "An unexpected error occurred: " + ExceptionUtils.getMessage(e);
            setErrorMessage(errorMsg);
            logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

}
