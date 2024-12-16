package com.testsigma.addons.web;

import com.testsigma.addons.utils.JSONUtilities;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.Data;

@Data
@Action(actionText = "Extract text from a JSON text test-data1 using the specified JSON path test-data2 and store it in a runtime variable variable_name",
        description = "Extract required json data using JSON string and JSON path, store data into a runtime variable",
        applicationType = ApplicationType.WEB)
public class ExtractTextUsingJsonPathInJson extends WebAction {

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
                String output = JSONUtilities.readJsonData(jsonString, jsonPath);
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(output);
                runTimeData.setKey(variable_name.getValue().toString());
                setSuccessMessage("Stored the desired date into the runtime variable. " + variable_name.getValue().toString() +
                        " = " + runTimeData.getValue());
                logger.info("Extracted Output: " + output);
            } catch (PathNotFoundException e) {
                setErrorMessage("Invalid JSON Path: " + jsonPath + ". Please verify the path.");
                logger.info("Invalid JSON Path: " + jsonPath);
                return Result.FAILED;
            } catch (Exception e) {
                setErrorMessage("An unexpected error occurred." + ExceptionUtils.getStackTrace(e));
                logger.info(ExceptionUtils.getStackTrace(e));
                return Result.FAILED;
            }
            return result;
        } catch (NullPointerException e) {
            logger.info("Null value encountered in test data or JSON path.");
            setErrorMessage("Null value encountered in test data or JSON path.");
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("An unexpected error occurred." + ExceptionUtils.getMessage(e));
            logger.info(ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

}
